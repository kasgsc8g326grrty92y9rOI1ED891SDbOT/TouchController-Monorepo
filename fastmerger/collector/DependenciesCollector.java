package top.fifthlight.fastmerger.collector;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import top.fifthlight.fastmerger.classdeps.ClassDependenciesVisitor;
import top.fifthlight.fastmerger.symbolmap.SymbolMap;

public class DependenciesCollector implements ClassDependenciesVisitor.Consumer {
    private final SymbolMap symbolMap;
    @Nullable
    private String className = null;
    private IntSet dependencies = new IntOpenHashSet();
    private boolean released = false;

    public DependenciesCollector(SymbolMap symbolMap) {
        this.symbolMap = symbolMap;
    }

    @Nullable
    public String getClassName() {
        return className;
    }

    @Override
    public void acceptClassDependency(String className, String dependencyName) {
        if (released) {
            throw new IllegalStateException("Already released");
        }
        if (this.className == null) {
            this.className = className;
        } else if (!this.className.equals(className)) {
            throw new IllegalStateException("Bad class name: current is " + this.className + ", but got " + className);
        }
        if (className.equals(dependencyName)) {
            return;
        }
        var symbolId = symbolMap.get(dependencyName);
        dependencies.add(symbolId);
    }

    public IntSet release() {
        if (released) {
            throw new IllegalStateException("Already released");
        }
        released = true;
        var dependencies = this.dependencies;
        this.dependencies = null;
        return dependencies;
    }
}
