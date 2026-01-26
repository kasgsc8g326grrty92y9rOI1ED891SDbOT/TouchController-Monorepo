package top.fifthlight.fastmerger.scanner;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import picocli.CommandLine;
import top.fifthlight.bazel.worker.api.Worker;
import top.fifthlight.fastmerger.bindeps.BindepsWriter;
import top.fifthlight.fastmerger.scanner.classdeps.ClassDepsScanner;
import top.fifthlight.fastmerger.scanner.classdeps.ClassNameMap;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

public class ScannerWorker extends Worker {

    @Command(name = "scanner", mixinStandardHelpOptions = true)
    public static class Handler implements Callable<Integer> {
        @Parameters(index = "0", description = "JAR file to be scanned")
        Path inputFile;

        @Parameters(index = "1", description = "Output binary dependencies file")
        Path outputFile;

        private final Path sandboxDir;

        public Handler(Path sandboxDir) {
            this.sandboxDir = sandboxDir;
        }

        private int[] lookupClassEntries(Object2IntMap<String> entryMap, ClassNameMap.Entry[] entries) {
            return Arrays.stream(entries)
                    .mapToInt(entry -> {
                        var index = entryMap.getInt(entry.fullName());
                        if (index == -1) {
                            throw new IllegalStateException("Entry not found for " + entry.fullName());
                        }
                        return index;
                    })
                    .sorted()
                    .toArray();
        }

        @Override
        public Integer call() throws Exception {
            var inputPath = sandboxDir.resolve(inputFile);
            var outputPath = sandboxDir.resolve(outputFile);

            var result = new ClassDepsScanner().scan(inputPath);

            var entries = new ArrayList<ClassNameMap.Entry>();
            var entryMap = new Object2IntOpenHashMap<String>();
            entryMap.defaultReturnValue(-1);
            var stack = new ArrayDeque<ClassNameMap.Entry>();
            result.classNameMap().rootEntries().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .forEachOrdered(entry -> stack.push(entry.getValue()));
            while (!stack.isEmpty()) {
                var entry = stack.pop();
                entryMap.put(entry.fullName(), entries.size());
                entries.add(entry);
                entry.entries().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                        .forEachOrdered(e -> stack.push(e.getValue()));
            }

            try (var writer = new BindepsWriter(outputPath, entries.size(), result.classInfos().size())) {
                for (var entry : entries) {
                    var parent = entry.parentEntry();
                    var parentIndex = parent != null ? entryMap.getInt(parent.fullName()) : -1;
                    writer.writeStringPoolEntry(entry.hash(), parentIndex, entry.nameBytes(), entry.fullNameBytes());
                }

                result.classInfos().values().stream()
                        .map(classInfo -> {
                            var nameIndex = entryMap.getInt(classInfo.getFullName());
                            return new IntObjectImmutablePair<>(nameIndex, classInfo);
                        })
                        .sorted(Comparator.comparingInt(IntObjectImmutablePair::leftInt))
                        .forEachOrdered(pair -> {
                            var classInfo = pair.right();
                            var superEntry = classInfo.superClass();
                            var superIndex = (superEntry != null) ? entryMap.getInt(superEntry.fullName()) : -1;

                            var interfaces = lookupClassEntries(entryMap, classInfo.interfaces());
                            var annotations = lookupClassEntries(entryMap, classInfo.annotations());
                            var dependencies = lookupClassEntries(entryMap, classInfo.dependencies());

                            writer.writeClassInfoEntry(pair.leftInt(), superIndex, classInfo.accessFlag(),
                                    interfaces, annotations, dependencies);
                        });
            }
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
