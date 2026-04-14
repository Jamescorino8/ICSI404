public class L2Cache {
    private Word32[][] data = new Word32[4][8]; // 4 lines, 8 words each
    private Word32[] baseAddresses = new Word32[4];
    private boolean[] valid = new boolean[4];
    private int nextEvict = 0; // round-robin eviction counter
    private Memory mem;

    public Word32 address = new Word32();
    public Word32 value = new Word32();
    public int lastCost = 0; // 20 if cache hit, 360 if cache miss

    public L2Cache(Memory m) {
        mem = m;
        for (int i = 0; i < 4; i++) {
            baseAddresses[i] = new Word32();
            data[i] = new Word32[8];
            for (int j = 0; j < 8; j++) {
                data[i][j] = new Word32();
            }
        }
    }

    public void read() {
        int addr = TestConverter.toInt(address);

        // Check all 4 lines for a hit
        for (int line = 0; line < 4; line++) {
            if (valid[line]) {
                int base = TestConverter.toInt(baseAddresses[line]);
                if (addr >= base && addr < base + 8) {
                    data[line][addr - base].copy(value);
                    lastCost = 20;
                    return;
                }
            }
        }

        // Miss — fill the next eviction line from main memory
        int alignedBase = addr - (addr % 8);
        int line = nextEvict;
        nextEvict = (nextEvict + 1) % 4;

        TestConverter.fromInt(alignedBase, baseAddresses[line]);
        for (int i = 0; i < 8; i++) {
            Word32 a = new Word32();
            TestConverter.fromInt(alignedBase + i, a);
            a.copy(mem.address);
            mem.read();
            mem.value.copy(data[line][i]);
        }

        valid[line] = true;
        data[line][addr - alignedBase].copy(value);
        lastCost = 360;
    }

    public void write() {
        int addr = TestConverter.toInt(address);
        // Check if address is in any line (write-through update)
        for (int line = 0; line < 4; line++) {
            if (valid[line]) {
                int base = TestConverter.toInt(baseAddresses[line]);
                if (addr >= base && addr < base + 8) {
                    // Hit — update L2 line and write through to memory
                    value.copy(data[line][addr - base]);
                    address.copy(mem.address);
                    value.copy(mem.value);
                    mem.write();
                    lastCost = 20;
                    return;
                }
            }
        }
        // Miss — write directly to main memory, don't allocate in cache
        address.copy(mem.address);
        value.copy(mem.value);
        mem.write();
        lastCost = 360;
    }

}