package top.fifthlight.fastmerger.bindeps;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BindepsReader {
    private final ByteBuffer dataBuffer;

    private final int stringPoolSize;
    private final int classInfoSize;

    public BindepsReader(Path inputPath) throws IOException {
        var buffer = ByteBuffer.allocateDirect(BindepsConstants.HEADER_SIZE);
        try (var channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) == -1) {
                    throw new IOException("Unexpected EOF in header");
                }
            }
            buffer.flip();

            for (var i = 0; i < BindepsConstants.MAGIC.length; i++) {
                var b = buffer.get();
                if (b != BindepsConstants.MAGIC[i]) {
                    throw new IOException("Invalid magic at byte %d: expected 0x%02X, got 0x%02X".formatted(i, BindepsConstants.MAGIC[i], b));
                }
            }

            var version = buffer.getInt();
            if (version != BindepsConstants.VERSION) {
                throw new IOException("Invalid version: expected %d, got %d".formatted(BindepsConstants.VERSION, version));
            }

            stringPoolSize = buffer.getInt();
            if (stringPoolSize < 0) {
                throw new IOException("Invalid string pool size: %d".formatted(stringPoolSize));
            }
            classInfoSize = buffer.getInt();
            if (classInfoSize < 0) {
                throw new IOException("Invalid class info size: %d".formatted(classInfoSize));
            }

            var heapSize = buffer.getInt();
            var dataSize = BindepsConstants.STRING_RECORD_SIZE * stringPoolSize + BindepsConstants.CLASS_RECORD_SIZE * classInfoSize + heapSize;

            ByteBuffer dataBuffer;
            try {
                dataBuffer = channel.map(FileChannel.MapMode.READ_ONLY, BindepsConstants.HEADER_SIZE, dataSize);
            } catch (IllegalArgumentException | UnsupportedOperationException | IOException e) {
                // Fallback to reading buffer into memory
                dataBuffer = ByteBuffer.allocateDirect(dataSize);
                channel.position(BindepsConstants.HEADER_SIZE);
                while (dataBuffer.hasRemaining()) {
                    if (channel.read(dataBuffer) == -1) {
                        throw new IOException("Unexpected EOF in data");
                    }
                }
                dataBuffer.flip();
            }
            this.dataBuffer = dataBuffer;
        }
    }

    private static String decodeCharBuffer(ByteBuffer buffer, int offset, int length) {
        return StandardCharsets.UTF_8.decode(buffer.slice(offset, length)).toString();
    }

    public static class StringPoolEntry {
        private final ByteBuffer buffer;
        private final int index;

        private final long hash;
        private final int parentIndex;
        private final int heapOffset;
        private final int nameLength;
        private final int fullNameLength;

        private StringPoolEntry parent = null;
        private String name = null;
        private String fullName = null;

        public StringPoolEntry(ByteBuffer buffer, int index) {
            this.buffer = buffer;
            this.index = index;
            var offset = index * BindepsConstants.STRING_RECORD_SIZE;
            this.hash = buffer.getLong(offset);
            this.parentIndex = buffer.getInt(offset + 8);
            this.heapOffset = buffer.getInt(offset + 12) - BindepsConstants.HEADER_SIZE;
            this.nameLength = Short.toUnsignedInt(buffer.getShort(offset + 16));
            this.fullNameLength = Short.toUnsignedInt(buffer.getShort(offset + 18));
        }

        public long getHash() {
            return hash;
        }

        public int getIndex() {
            return index;
        }

        public int getParentIndex() {
            return parentIndex;
        }

        public StringPoolEntry getParent() {
            if (parent == null) {
                parent = new StringPoolEntry(buffer, parentIndex);
            }
            return parent;
        }

        public String getName() {
            if (name == null) {
                name = decodeCharBuffer(buffer, heapOffset, nameLength);
            }
            return name;
        }

        public String getFullName() {
            if (fullName == null) {
                fullName = decodeCharBuffer(buffer, heapOffset + nameLength, fullNameLength);
            }
            return fullName;
        }
    }

    public int getStringPoolSize() {
        return stringPoolSize;
    }

    public StringPoolEntry getStringPoolEntry(int index) {
        return new StringPoolEntry(dataBuffer, index);
    }

    public static class ClassInfoEntry {
        private final ByteBuffer buffer;
        private final int index;

        private final int nameIndex;
        private final int superIndex;
        private final int access;
        private final int interfaceOffset;
        private final int interfaceCount;
        private final int annotationOffset;
        private final int annotationCount;
        private final int dependenciesOffset;
        private final int dependenciesCount;

        private StringPoolEntry name = null;
        private StringPoolEntry superClass = null;
        private int[] interfaceIndices = null;
        private int[] annotationIndices = null;
        private int[] dependenciesIndices = null;
        private StringPoolEntry[] interfaces = null;
        private StringPoolEntry[] annotations = null;
        private StringPoolEntry[] dependencies = null;

        public ClassInfoEntry(ByteBuffer buffer, int classInfoOffset, int index) {
            this.buffer = buffer;
            this.index = index;
            var offset = classInfoOffset + index * BindepsConstants.CLASS_RECORD_SIZE;
            nameIndex = buffer.getInt(offset);
            superIndex = buffer.getInt(offset + 4);
            access = buffer.getInt(offset + 8);
            interfaceOffset = buffer.getInt(offset + 12);
            interfaceCount = buffer.getInt(offset + 16);
            annotationOffset = buffer.getInt(offset + 20);
            annotationCount = buffer.getInt(offset + 24);
            dependenciesOffset = buffer.getInt(offset + 28);
            dependenciesCount = buffer.getInt(offset + 32);
        }

        public int getIndex() {
            return index;
        }

        public int getNameIndex() {
            return nameIndex;
        }

        public StringPoolEntry getName() {
            if (name == null) {
                name = new StringPoolEntry(buffer, nameIndex);
            }
            return name;
        }

        public int getSuperIndex() {
            return superIndex;
        }

        @Nullable
        public StringPoolEntry getSuperClass() {
            if (superIndex == -1) {
                return null;
            }
            if (superClass == null) {
                superClass = new StringPoolEntry(buffer, superIndex);
            }
            return superClass;
        }

        public int getAccess() {
            return access;
        }

        public int[] getInterfaceIndices() {
            if (interfaceIndices == null) {
                if (interfaceCount == 0) {
                    interfaces = new StringPoolEntry[0];
                } else {
                    interfaceIndices = new int[interfaceCount];
                    buffer.slice(interfaceOffset - BindepsConstants.HEADER_SIZE, interfaceCount * 4)
                            .asIntBuffer()
                            .get(interfaceIndices);
                }
            }
            return interfaceIndices;
        }

        public StringPoolEntry[] getInterfaces() {
            if (interfaces == null) {
                var interfaceIndices = getInterfaceIndices();
                interfaces = new StringPoolEntry[interfaceCount];
                for (var i = 0; i < interfaceCount; i++) {
                    interfaces[i] = new StringPoolEntry(buffer, interfaceIndices[i]);
                }
            }
            return interfaces;
        }

        public int[] getAnnotationIndices() {
            if (annotationIndices == null) {
                if (annotationCount == 0) {
                    annotationIndices = new int[0];
                } else {
                    annotationIndices = new int[annotationCount];
                    buffer.slice(annotationOffset - BindepsConstants.HEADER_SIZE, annotationCount * 4)
                            .asIntBuffer()
                            .get(annotationIndices);
                }
            }
            return annotationIndices;
        }

        public StringPoolEntry[] getAnnotations() {
            if (annotations == null) {
                var annotationIndices = getAnnotationIndices();
                annotations = new StringPoolEntry[annotationCount];
                for (var i = 0; i < annotationCount; i++) {
                    annotations[i] = new StringPoolEntry(buffer, annotationIndices[i]);
                }
            }
            return annotations;
        }

        public int[] getDependenciesIndices() {
            if (dependenciesIndices == null) {
                if (dependenciesCount == 0) {
                    dependenciesIndices = new int[0];
                } else {
                    dependenciesIndices = new int[dependenciesCount];
                    buffer.slice(dependenciesOffset - BindepsConstants.HEADER_SIZE, dependenciesCount * 4)
                            .asIntBuffer()
                            .get(dependenciesIndices);
                }
            }
            return dependenciesIndices;
        }

        public StringPoolEntry[] getDependencies() {
            if (dependencies == null) {
                var dependenciesIndices = getDependenciesIndices();
                dependencies = new StringPoolEntry[dependenciesCount];
                for (var i = 0; i < dependenciesCount; i++) {
                    dependencies[i] = new StringPoolEntry(buffer, dependenciesIndices[i]);
                }
            }
            return dependencies;
        }
    }

    public int getClassInfoSize() {
        return classInfoSize;
    }

    public ClassInfoEntry getClassInfoEntry(int index) {
        return new ClassInfoEntry(dataBuffer, BindepsConstants.STRING_RECORD_SIZE * stringPoolSize, index);
    }
}
