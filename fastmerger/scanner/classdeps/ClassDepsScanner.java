package top.fifthlight.fastmerger.scanner.classdeps;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ClassDepsScanner {
    public record Result(PathMap.Result pathMap, Map<String, ClassInfo> classInfos) {
    }

    public Result scan(Path jarPath) throws IOException {
        var pathMap = new PathMap();
        var classInfos = new HashMap<String, ClassInfo>();
        var maxConcurrency = Runtime.getRuntime().availableProcessors();
        try (var executor = new ThreadPoolExecutor(
                maxConcurrency,
                maxConcurrency,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxConcurrency),
                new ThreadPoolExecutor.CallerRunsPolicy()
        )) {
            var futures = new ArrayList<Future<ClassInfo>>();
            try (var zipFile = ZipFile.builder().setPath(jarPath).get()) {
                var entries = zipFile.getEntriesInPhysicalOrder();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    var name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    byte[] content;
                    try (var stream = zipFile.getInputStream(entry)) {
                        content = stream.readAllBytes();
                    }
                    futures.add(executor.submit(() -> {
                        var classReader = new ClassReader(content);
                        var collector = new ClassInfoCollector(pathMap);
                        classReader.accept(new ClassInfoVisitor(collector), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        return collector.getClassInfo();
                    }));
                }
                for (var future : futures) {
                    var classInfo = future.get();
                    classInfos.put(classInfo.getFullName(), classInfo);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdownNow();
            }
        }
        return new Result(pathMap.finish(), classInfos);
    }
}
