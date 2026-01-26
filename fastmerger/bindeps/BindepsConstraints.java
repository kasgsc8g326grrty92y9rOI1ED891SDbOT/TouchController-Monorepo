package top.fifthlight.fastmerger.bindeps;

public class BindepsConstraints {
    private BindepsConstraints() {}

    public static final byte[] MAGIC = new byte[]{0x42, 0x49, 0x4E, 0x44, 0x45, 0x50, 0x53, 0x03};
    public static final int VERSION = 1;

    public static final int STRING_RECORD_SIZE = 24; // 8 (hash) + 4(parent) + 4(offset) + 2(name len) + 2(full name len) + 4(padding)
    public static final int CLASS_RECORD_SIZE = 36;  // 9 ints: name, super, access + 3 x (offset, count)
}
