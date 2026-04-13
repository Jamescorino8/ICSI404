public class instructionCache {
    private Word32[] data = new Word32[8]; // the 8 cached words
    private Word32 baseAddress; // address of data[0]
    private boolean valid = false; // true if cache has been filled
    private Memory mem;

    public Word32 address = new Word32(); 
    public Word32 value = new Word32();
    public int lastCost = 0; // 10 if cache hit, 350 if cache miss

    public instructionCache(Memory m) {
        mem = m;
        baseAddress = new Word32();
        for (int i = 0; i < 8; i++) {
            data[i] = new Word32();
        }
    }

    public void read() {
        int addr = TestConverter.toInt(address);
        int base = valid ? TestConverter.toInt(baseAddress) : -1;

        if (valid && addr >= base && addr < base + 8) {
            // cache hit
            data[addr - base].copy(value);
            lastCost = 10;
        } else {
            // cache miss
            int alignedBase = addr - (addr % 8);
            TestConverter.fromInt(alignedBase, baseAddress);

            for (int i = 0; i < 8; i++) {
                Word32 a = new Word32();
                TestConverter.fromInt(alignedBase + 1, a);
                a.copy(mem.address);
                mem.read();
                mem.value.copy(data[i]);
            }

            valid = true;
            data[addr - alignedBase].copy(value);
            lastCost = 350;
        }
    }
}
