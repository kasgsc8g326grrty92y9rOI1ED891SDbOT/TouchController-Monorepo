package top.fifthlight.fastmerger;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.objectweb.asm.ClassReader;
import top.fifthlight.fastmerger.classdeps.ClassDependenciesVisitor;
import top.fifthlight.fastmerger.collector.DependenciesCollector;
import top.fifthlight.fastmerger.symbolmap.SymbolMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        try (var jis = new JarInputStream(new FileInputStream(args[0]))) {
            var symbolMap = new SymbolMap();
            var dependenciesMap = new Int2ObjectOpenHashMap<IntSet>();

            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!entry.getName().toLowerCase(Locale.ROOT).endsWith(".class")) {
                    continue;
                }
                var classReader = new ClassReader(jis);
                var collector = new DependenciesCollector(symbolMap);
                var depsVisitor = new ClassDependenciesVisitor(collector);
                classReader.accept(depsVisitor, 0);

                var classNameIndex = symbolMap.get(collector.getClassName());
                var dependencies = collector.release();
                dependenciesMap.put(classNameIndex, dependencies);
            }

            var symbolResult = symbolMap.release();
            for (var classEntry : dependenciesMap.int2ObjectEntrySet()) {
                var className = symbolResult.ids().get(classEntry.getIntKey());
                System.out.println(className);
                var idSet = classEntry.getValue();
                for (var id : idSet) {
                    var dependency = symbolResult.ids().get(id.intValue());
                    System.out.print("\t");
                    System.out.println(dependency);
                }
            }
        }
    }
}