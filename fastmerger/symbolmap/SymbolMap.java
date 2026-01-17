package top.fifthlight.fastmerger.symbolmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe container for String -> int mapping.
 */
public class SymbolMap {
    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicBoolean released = new AtomicBoolean(false);

    public int get(String symbol) {
        if (released.get()) {
            throw new IllegalStateException("SymbolMap already released");
        }
        return map.computeIfAbsent(symbol, k -> counter.incrementAndGet());
    }

    public record Result(Object2IntMap<String> symbols, Int2ObjectMap<String> ids) {}

    public Result release() {
        if (!released.compareAndSet(false, true)) {
            throw new IllegalStateException("SymbolMap already released");
        }
        var symbols = new Object2IntOpenHashMap<String>();
        var ids = new Int2ObjectOpenHashMap<String>();
        for (var entry : map.entrySet()) {
            symbols.put(entry.getKey(), entry.getValue().intValue());
            ids.put(entry.getValue().intValue(), entry.getKey());
        }
        map = null;
        return new Result(symbols, ids);
    }
}
