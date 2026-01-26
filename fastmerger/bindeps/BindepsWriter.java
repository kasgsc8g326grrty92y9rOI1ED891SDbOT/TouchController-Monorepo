package top.fifthlight.fastmerger.bindeps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BindepsWriter implements AutoCloseable {
    private boolean closed = false;
    private static final int BUFFER_SIZE = 256 * 1024;
    private ByteBuffer indexBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private ByteBuffer heapBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final FileChannel outputChannel;

    private int currentHeapOffset;

    public BindepsWriter(Path outputPath, int stringPoolSize, int classInfoSize) throws IOException {
        this.outputChannel = FileChannel.open(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        indexBuffer.order(ByteOrder.BIG_ENDIAN);
        heapBuffer.order(ByteOrder.BIG_ENDIAN);

        indexBuffer.put(BindepsConstants.MAGIC);
        indexBuffer.putInt(BindepsConstants.VERSION);
        indexBuffer.putInt(stringPoolSize);
        indexBuffer.putInt(classInfoSize);
        indexBuffer.put(new byte[4]); // Pad header to 24 bytes

        currentHeapOffset = BindepsConstants.HEADER_SIZE + BindepsConstants.STRING_RECORD_SIZE * stringPoolSize + BindepsConstants.CLASS_RECORD_SIZE * classInfoSize;
    }

    private ByteBuffer expandBuffer(ByteBuffer buffer, int minRemaining) {
        var newSize = Math.max(buffer.capacity() * 2, buffer.capacity() + minRemaining);
        buffer.flip();
        var newBuffer = ByteBuffer.allocateDirect(newSize);
        newBuffer.order(ByteOrder.BIG_ENDIAN);
        newBuffer.put(buffer);
        return newBuffer;
    }

    private void ensureIndexBufferSize(int size) {
        if (indexBuffer.remaining() < size) {
            indexBuffer = expandBuffer(indexBuffer, size);
        }
    }

    private void ensureHeapBufferSize(int size) {
        if (heapBuffer.remaining() < size) {
            heapBuffer = expandBuffer(heapBuffer, size);
        }
    }

    public void writeStringPoolEntry(long hash, int parentIndex, byte[] nameBytes, byte[] fullNameBytes) {
        if (closed) {
            throw new IllegalStateException("Writer is closed");
        }
        var nameLength = nameBytes.length;
        var fullNameLength = fullNameBytes.length;

        // Write index
        ensureIndexBufferSize(BindepsConstants.STRING_RECORD_SIZE);
        indexBuffer.putLong(hash);
        indexBuffer.putInt(parentIndex);
        indexBuffer.putInt(currentHeapOffset);
        indexBuffer.putShort((short) nameLength);
        indexBuffer.putShort((short) fullNameLength);
        indexBuffer.put(new byte[4]); // Padded to 24 bytes

        // Write heap
        ensureHeapBufferSize(nameLength + fullNameLength);
        heapBuffer.put(nameBytes);
        heapBuffer.put(fullNameBytes);
        currentHeapOffset += nameLength + fullNameLength;
    }

    public void writeClassInfoEntry(int nameIndex, int superIndex, int access,
                                    int[] interfaces, int[] annotations, int[] dependencies) {
        if (closed) {
            throw new IllegalStateException("Writer is closed");
        }

        // Write heap
        var interfaceOffset = writeIntArrayToHeap(interfaces);
        var annotationOffset = writeIntArrayToHeap(annotations);
        var dependenciesOffset = writeIntArrayToHeap(dependencies);

        // Write index
        ensureIndexBufferSize(BindepsConstants.CLASS_RECORD_SIZE);
        indexBuffer.putInt(nameIndex);
        indexBuffer.putInt(superIndex);
        indexBuffer.putInt(access);

        indexBuffer.putInt(interfaceOffset);
        indexBuffer.putInt(interfaces.length);

        indexBuffer.putInt(annotationOffset);
        indexBuffer.putInt(annotations.length);

        indexBuffer.putInt(dependenciesOffset);
        indexBuffer.putInt(dependencies.length);

        indexBuffer.put(new byte[12]); // Padded to 48 bytes
    }

    private int writeIntArrayToHeap(int[] array) {
        if (array.length == 0) {
            return -1;
        }

        var startOffset = currentHeapOffset;
        var byteLen = array.length * 4;

        ensureHeapBufferSize(byteLen);
        for (var i : array) {
            heapBuffer.putInt(i);
        }

        currentHeapOffset += byteLen;
        return startOffset;
    }

    private void writeBuffer(ByteBuffer buffer) throws IOException {
        buffer.flip();
        while (buffer.hasRemaining()) {
            outputChannel.write(buffer);
        }
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            writeBuffer(indexBuffer);
            writeBuffer(heapBuffer);
        } finally {
            outputChannel.close();
            closed = true;
        }
    }
}
