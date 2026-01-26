package top.fifthlight.fastmerger.bindeps;

public class BindepsConstants {
    private BindepsConstants() {}

    public static final byte[] MAGIC = new byte[]{0x42, 0x49, 0x4E, 0x44, 0x45, 0x50, 0x53, 0x03};
    public static final int VERSION = 1;

    public static final int HEADER_SIZE = 24; // 8(magic) + 4(version) + 4(string pool size) + 4(class info size) + 4(heap size)
    public static final int STRING_RECORD_SIZE = 24; // 8 (hash) + 4(parent) + 4(offset) + 2(name len) + 2(full name len) + 4(padding)
    public static final int CLASS_RECORD_SIZE = 48;  // 9 ints: name, super, access + 3 x (offset, count) + 12(padding)
}
