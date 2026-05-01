public class InstructionCache {
    private Word32[] data = new Word32[8]; // the 8 cached words
    private Word32 baseAddress; // address of data[0]
    private boolean valid = false; // true if cache has been filled
    public Word32 address = new Word32();
    public Word32 value = new Word32();
    public int lastCost = 0; // 10 if hit; 30 or 370 via L2; 350 direct to memory
    private L2Cache l2;   // non-null when using L2 (modes 3/4)
    private Memory mem;   // non-null when filling directly from memory (mode 2)

    public InstructionCache(L2Cache l2) {
        this.l2 = l2;
        baseAddress = new Word32();
        for (int i = 0; i < 8; i++) data[i] = new Word32();
    }

    // Mode 2: IC fills directly from main memory (no L2), miss cost = 350
    public InstructionCache(Memory mem) {
        this.mem = mem;
        baseAddress = new Word32();
        for (int i = 0; i < 8; i++) data[i] = new Word32();
    }

    public void read() {
        int addr = TestConverter.toInt(address);
        int base = valid ? TestConverter.toInt(baseAddress) : -1;

        if (valid && addr >= base && addr < base + 8) {
            data[addr - base].copy(value);
            lastCost = 10;
        } else {
            int alignedBase = addr - (addr % 8);
            TestConverter.fromInt(alignedBase, baseAddress);

            if (l2 != null) {
                // Miss via L2 (modes 3/4): cost = 10 + L2 cost
                address.copy(l2.address);
                l2.read();
                lastCost = 10 + l2.lastCost;
                for (int i = 0; i < 8; i++) {
                    Word32 a = new Word32();
                    TestConverter.fromInt(alignedBase + i, a);
                    a.copy(l2.address);
                    l2.read();
                    l2.value.copy(data[i]);
                }
            } else {
                // Miss direct to memory (mode 2): cost = 350
                for (int i = 0; i < 8; i++) {
                    Word32 a = new Word32();
                    TestConverter.fromInt(alignedBase + i, a);
                    a.copy(mem.address);
                    mem.read();
                    mem.value.copy(data[i]);
                }
                lastCost = 350;
            }

            valid = true;
            data[addr - alignedBase].copy(value);
        }
    }
}
