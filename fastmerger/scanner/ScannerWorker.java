package top.fifthlight.fastmerger.scanner;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import picocli.CommandLine;
import top.fifthlight.bazel.worker.api.Worker;
import top.fifthlight.fastmerger.bindeps.BindepsConstants;
import top.fifthlight.fastmerger.bindeps.BindepsWriter;
import top.fifthlight.fastmerger.scanner.classdeps.ClassDepsScanner;
import top.fifthlight.fastmerger.scanner.classdeps.ClassInfo;
import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static picocli.CommandLine.*;

public class ScannerWorker extends Worker {
    private static class Environment {
        private final Path inputPath;
        private final Path outputPath;
        private boolean scanned = false;

        private Environment(Path inputPath, Path outputPath) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
        }

        private record ClassInfoEntry(ClassInfo classInfo, ResourceInfo resourceInfo) {
        }

        private final PathMap pathMap = new PathMap();
        private final Map<String, ResourceInfo> resourceInfoNameMap = new HashMap<>();
        private final Map<String, ClassInfoEntry> classInfoNameMap = new HashMap<>();

        private static int getFlag(ZipArchiveEntry entry, String entryName) {
            var flag = 0;
            if (entryName.endsWith(".class")) {
                flag = flag | BindepsConstants.RESOURCE_FLAG_CLASS;
            } else {
                flag = flag | BindepsConstants.RESOURCE_FLAG_RESOURCE;
                var isMetaInfo = entryName.startsWith("META-INF/");
                var isAndroidAarRule = entry.getName().equals("proguard.txt");
                var isProguardFile = entry.getName().startsWith("META-INF/proguard/") && entry.getName().endsWith(".pro");
                var isSignature = entryName.endsWith(".SF") || entryName.endsWith(".RSA") || entryName.endsWith(".DSA");
                if (entryName.equals("META-INF/MANIFEST.MF")) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_MANIFEST;
                } else if (entryName.startsWith("META-INF/services/")) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_SPI;
                    flag = flag | BindepsConstants.RESOURCE_FLAG_INLINE;
                } else if (isAndroidAarRule || isProguardFile) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_PROGUARD;
                    flag = flag | BindepsConstants.RESOURCE_FLAG_INLINE;
                } else if (isMetaInfo && isSignature) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_SIGNATURE;
                }
            }
            return flag;
        }

        private ResourceInfo scanResource(ZipFile zipFile, ZipArchiveEntry entry) throws IOException {
            var entryName = entry.getName();
            var flag = getFlag(entry, entryName);
            var name = pathMap.getOrCreate(entryName);
            var crc32 = (int) entry.getCrc();

            var entryMethod = entry.getMethod();
            var compressMethod = switch (entry.getMethod()) {
                case ZipArchiveEntry.STORED, ZipArchiveEntry.DEFLATED -> (short) entryMethod;
                default -> throw new IllegalStateException("Unsupported compress method " +
                        entryMethod + " for entry: " + entry.getName());
            };

            int dataOffset;
            int compressedSize;
            int uncompressedSize;
            byte[] data = null;
            if ((flag & BindepsConstants.RESOURCE_FLAG_INLINE) == 0) {
                var dataOffsetLong = entry.getDataOffset();
                if (dataOffsetLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Data offset too large for entry: " + entry.getName());
                }
                dataOffset = (int) dataOffsetLong;

                var compressedSizeLong = entry.getCompressedSize();
                if (compressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Compressed size too large for entry: " + entry.getName());
                }
                compressedSize = (int) compressedSizeLong;

                var uncompressedSizeLong = entry.getSize();
                if (uncompressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Uncompressed size too large for entry: " + entry.getName());
                }
                uncompressedSize = (int) uncompressedSizeLong;
            } else {
                dataOffset = -1;

                var uncompressedSizeLong = entry.getSize();
                if (uncompressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Uncompressed size too large for entry: " + entry.getName());
                }
                compressedSize = (int) uncompressedSizeLong;
                uncompressedSize = (int) uncompressedSizeLong;

                try (var stream = zipFile.getInputStream(entry)) {
                    data = stream.readNBytes(uncompressedSize);
                }
            }

            return new ResourceInfo(flag, name, crc32, dataOffset, compressedSize, uncompressedSize, compressMethod, data);
        }

        private ClassInfoEntry scanClassInfo(ResourceInfo resourceInfo, byte[] entry) {
            var classInfo = ClassDepsScanner.scan(pathMap, entry);
            return new ClassInfoEntry(classInfo, resourceInfo);
        }

        private void scanJar() throws Exception {
            var maxConcurrency = Runtime.getRuntime().availableProcessors();
            try (var executor = new ThreadPoolExecutor(
                    maxConcurrency,
                    maxConcurrency,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxConcurrency),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            )) {
                var futures = new ArrayList<CompletableFuture<ClassInfoEntry>>();
                try (var zipFile = ZipFile.builder().setPath(inputPath).get()) {
                    var entries = zipFile.getEntriesInPhysicalOrder();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        if (entry.isDirectory()) {
                            continue;
                        }

                        var resourceEntry = scanResource(zipFile, entry);
                        resourceInfoNameMap.put(resourceEntry.name().fullName(), resourceEntry);

                        var name = entry.getName();
                        if (!name.endsWith(".class")) {
                            continue;
                        }
                        byte[] content;
                        try (var stream = zipFile.getInputStream(entry)) {
                            content = stream.readAllBytes();
                        }
                        futures.add(CompletableFuture.supplyAsync(() -> scanClassInfo(resourceEntry, content), executor));
                    }
                    for (var future : futures) {
                        var entry = future.get();
                        classInfoNameMap.put(entry.classInfo.getFullName(), entry);
                    }
                } finally {
                    executor.shutdownNow();
                }
            }
        }

        private ArrayList<PathMap.Entry> pathMapEntries;
        private Object2IntOpenHashMap<PathMap.Entry> pathMapEntriesIndexMap;

        private void sortPathMap() {
            var pathMapResult = pathMap.finish();
            pathMapEntries = new ArrayList<>(pathMapResult.size());
            pathMapEntriesIndexMap = new Object2IntOpenHashMap<>(pathMapResult.size());
            pathMapEntriesIndexMap.defaultReturnValue(-1);
            var stack = new ArrayDeque<PathMap.Entry>();
            pathMapResult.rootEntries().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .forEachOrdered(entry -> stack.push(entry.getValue()));

            while (!stack.isEmpty()) {
                var entry = stack.pop();
                pathMapEntriesIndexMap.put(entry, pathMapEntries.size());
                pathMapEntries.add(entry);
                entry.entries().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                        .forEachOrdered(e -> stack.push(e.getValue()));
            }
        }

        private void writeStringMap(BindepsWriter writer) {
            for (var entry : pathMapEntries) {
                var parent = entry.parentEntry();
                var parentIndex = parent != null ? pathMapEntriesIndexMap.getInt(parent.fullName()) : -1;
                writer.writeStringPoolEntry(entry.hash(), parentIndex, entry.nameBytes(), entry.fullNameBytes());
            }
        }

        private ArrayList<ResourceInfo> resourceInfos;
        private Object2IntOpenHashMap<ResourceInfo> resourceInfoIndexMap;

        private void sortResourceInfos() {
            resourceInfos = new ArrayList<>(resourceInfoNameMap.size());
            resourceInfoIndexMap = new Object2IntOpenHashMap<>(resourceInfoNameMap.size());
            resourceInfoIndexMap.defaultReturnValue(-1);
            resourceInfoNameMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(entry -> {
                        resourceInfoIndexMap.put(entry.getValue(), resourceInfos.size());
                        resourceInfos.add(entry.getValue());
                    });
        }

        private void writeResourceInfos(BindepsWriter writer) {
            for (var resourceInfo : resourceInfos) {
                var nameIndex = pathMapEntriesIndexMap.getInt(resourceInfo.name());
                if (nameIndex == -1) {
                    throw new IllegalStateException("Name not found for " + resourceInfo.name().fullName());
                }
                writer.writeResourceEntry(resourceInfo.flag(), nameIndex, resourceInfo.crc32(),
                        resourceInfo.dataOffset(), resourceInfo.compressedSize(), resourceInfo.uncompressedSize(),
                        resourceInfo.compressMethod(), resourceInfo.data());
            }
        }

        private int[] lookupClassEntries(Object2IntMap<PathMap.Entry> entryMap, PathMap.Entry[] entries) {
            return Arrays.stream(entries)
                    .mapToInt(entry -> {
                        var index = entryMap.getInt(entry);
                        if (index == -1) {
                            throw new IllegalStateException("Entry not found for " + entry.fullName());
                        }
                        return index;
                    })
                    .sorted()
                    .toArray();
        }

        private void writeClassInfo(BindepsWriter writer) {
            classInfoNameMap.values().stream()
                    .map(classInfo -> {
                        var nameIndex = pathMapEntriesIndexMap.getInt(classInfo.classInfo().entry());
                        return new IntObjectImmutablePair<>(nameIndex, classInfo);
                    })
                    .sorted(Comparator.comparingInt(IntObjectImmutablePair::leftInt))
                    .forEachOrdered(pair -> {
                        var resourceInfo = pair.right().resourceInfo();
                        var resourceIndex = resourceInfoIndexMap.getInt(resourceInfo);
                        if (resourceIndex == -1) {
                            throw new IllegalStateException("Resource index not found for " + resourceInfo.name().fullName());
                        }

                        var classInfo = pair.right().classInfo();
                        var superEntry = classInfo.superClass();
                        var superIndex = (superEntry != null) ? pathMapEntriesIndexMap.getInt(superEntry.fullName()) : -1;

                        var interfaces = lookupClassEntries(pathMapEntriesIndexMap, classInfo.interfaces());
                        var annotations = lookupClassEntries(pathMapEntriesIndexMap, classInfo.annotations());
                        var dependencies = lookupClassEntries(pathMapEntriesIndexMap, classInfo.dependencies());

                        writer.writeClassInfoEntry(pair.leftInt(), superIndex, classInfo.accessFlag(),
                                interfaces, annotations, dependencies, resourceIndex);
                    });
        }

        private void writeOutput() throws IOException {
            sortPathMap();
            sortResourceInfos();
            try (var writer = new BindepsWriter(outputPath, pathMapEntries.size(), resourceInfos.size(), classInfoNameMap.size())) {
                writeStringMap(writer);
                writeResourceInfos(writer);
                writeClassInfo(writer);
            }
        }

        public void scan() throws Exception {
            if (scanned) {
                throw new IllegalStateException("Already scanned");
            }
            scanned = true;

            scanJar();
            writeOutput();
        }
    }

    @Command(name = "scanner", mixinStandardHelpOptions = true)
    private static class Handler implements Callable<Integer> {
        @Parameters(index = "0", description = "JAR file to be scanned")
        Path inputFile;

        @Parameters(index = "1", description = "Output binary dependencies file")
        Path outputFile;

        private final Path sandboxDir;

        public Handler(Path sandboxDir) {
            this.sandboxDir = sandboxDir;
        }

        @Override
        public Integer call() throws Exception {
            var inputPath = sandboxDir.resolve(inputFile);
            var outputPath = sandboxDir.resolve(outputFile);
            var environment = new Environment(inputPath, outputPath);
            environment.scan();
            return 0;
        }
    }

    @Override
    protected int handleRequest(PrintWriter out, Path sandboxDir, String... args) throws Exception {
        var wrapper = new Handler(sandboxDir);
        var commandLine = new CommandLine(wrapper);
        commandLine.setOut(out);
        commandLine.setErr(out);
        return commandLine.execute(args);
    }

    public static void main(String[] args) throws Exception {
        new ScannerWorker().run(args);
    }
}
