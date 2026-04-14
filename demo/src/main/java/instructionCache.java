public class InstructionCache {
    private Word32[] data = new Word32[8]; // the 8 cached words
    private Word32 baseAddress; // address of data[0]
    private boolean valid = false; // true if cache has been filled
    public Word32 address = new Word32(); 
    public Word32 value = new Word32();
    public int lastCost = 0; // 10 if cache hit, 350 if cache miss
    private L2Cache l2;

    public InstructionCache(L2Cache l2) {
        this.l2 = l2;
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
            address.copy(l2.address);
            l2.read();
            lastCost = 10 + l2.lastCost;

            int alignedBase = addr - (addr % 8);
            TestConverter.fromInt(alignedBase, baseAddress);
            for (int i = 0; i < 8; i++) {    
                Word32 a = new Word32();
                TestConverter.fromInt(alignedBase + i, a);
                a.copy(l2.address);
                l2.read();
                l2.value.copy(data[i]);
            }

            valid = true;
            data[addr - alignedBase].copy(value);
        }
    }
}
