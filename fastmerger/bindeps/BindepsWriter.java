package top.fifthlight.fastmerger.bindeps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BindepsWriter implements AutoCloseable {
    private static final int BUFFER_SIZE = 256 * 1024;
    private final ByteBuffer indexBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final ByteBuffer heapBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final FileChannel indexChannel;
    private final FileChannel heapChannel;

    private int currentHeapOffset = 0;

    public BindepsWriter(Path indexPath, Path heapPath, int stringPoolSize, int classInfoSize) throws IOException {
        this.indexChannel = FileChannel.open(indexPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        this.heapChannel = FileChannel.open(heapPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        indexBuffer.order(ByteOrder.BIG_ENDIAN);
        heapBuffer.order(ByteOrder.BIG_ENDIAN);

        indexBuffer.put(BindepsConstraints.MAGIC);
        indexBuffer.putInt(BindepsConstraints.VERSION);
        indexBuffer.putInt(stringPoolSize);
        indexBuffer.putInt(classInfoSize);
    }

    private void flushIfNeeded(FileChannel channel, ByteBuffer buf, int size) throws IOException {
        if (buf.remaining() < size) {
            flush(channel, buf);
        }
    }

    private void flush(FileChannel channel, ByteBuffer buf) throws IOException {
        buf.flip();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
        buf.clear();
    }

    public void writeStringPoolEntry(long hash, int parentIndex, byte[] nameBytes, byte[] fullNameBytes) throws IOException {
        var nameLength = nameBytes.length;
        var fullNameLength = fullNameBytes.length;

        // Write index
        flushIfNeeded(indexChannel, indexBuffer, BindepsConstraints.STRING_RECORD_SIZE);
        indexBuffer.putLong(hash);
        indexBuffer.putInt(parentIndex);
        indexBuffer.putInt(currentHeapOffset);
        indexBuffer.putShort((short) nameLength);
        indexBuffer.putShort((short) fullNameLength);
        indexBuffer.put(new byte[4]); // Padded to 24 bytes

        // Write heap
        flushIfNeeded(heapChannel, heapBuffer, nameLength + fullNameLength);
        heapBuffer.put(nameBytes);
        heapBuffer.put(fullNameBytes);
        currentHeapOffset += nameLength + fullNameLength;
    }

    public void writeClassInfoEntry(int nameIndex, int superIndex, int access,
                                    int[] interfaces, int[] annotations, int[] dependencies) throws IOException {
        // Write heap
        var interfaceOffset = writeIntArrayToHeap(interfaces);
        var annotationOffset = writeIntArrayToHeap(annotations);
        var dependenciesOffset = writeIntArrayToHeap(dependencies);

        // Write index
        flushIfNeeded(indexChannel, indexBuffer, BindepsConstraints.CLASS_RECORD_SIZE);
        indexBuffer.putInt(nameIndex);
        indexBuffer.putInt(superIndex);
        indexBuffer.putInt(access);

        indexBuffer.putInt(interfaceOffset);
        indexBuffer.putInt(interfaces.length);

        indexBuffer.putInt(annotationOffset);
        indexBuffer.putInt(annotations.length);

        indexBuffer.putInt(dependenciesOffset);
        indexBuffer.putInt(dependencies.length);
    }

    private int writeIntArrayToHeap(int[] array) throws IOException {
        if (array.length == 0) return -1;

        var startOffset = currentHeapOffset;
        var byteLen = array.length * 4;

        flushIfNeeded(heapChannel, heapBuffer, byteLen);
        for (var i : array) {
            heapBuffer.putInt(i);
        }

        currentHeapOffset += byteLen;
        return startOffset;
    }

    @Override
    public void close() throws IOException {
        flush(indexChannel, indexBuffer);
        flush(heapChannel, heapBuffer);
        indexChannel.close();
        heapChannel.close();
    }
}