public class Memory {
    public Word32 address= new Word32();
    public Word32 value = new Word32();

    private final Word32[] dram = new Word32[1000];

    public int addressAsInt() { 
        int addressAsInt = 0;
        for (int i = 0, power = 31; i < 32; i++, power--) {
            Bit currentBit = new Bit(false);
            address.getBitN(i, currentBit);
            if (currentBit.getValue() == true) {
                addressAsInt += 1 * Math.pow(2, power);
            }
        }
        return addressAsInt;
    }

    public Memory() {

    }

    public void read() {
        int index = addressAsInt();
        if (index < 0 || index >= dram.length) throw new IndexOutOfBoundsException("Address out of bounds: " + index);
        if (dram[index] != null) {
            dram[index].copy(value);
        }
    }

    public void write() {
        int index = addressAsInt();
        if (index < 0 || index >= dram.length) throw new IndexOutOfBoundsException("Address out of bounds: " + index);
        if (dram[index] == null) {
            dram[index] = new Word32();
        }
        value.copy(dram[index]);
    }
        
    public void load(String[] data) {
        for (int i = 0; i < data.length; i++) {
            String line = data[i];
            if (line.length() != 32) throw new IllegalArgumentException("Each string must be exactly 32 characters");
            
            // Create Word32 from String
            Word32 word = new Word32();
            for (int j = 0; j < 32; j++) {
                char c = line.charAt(j);
                if (c != '0' && c != '1') throw new IllegalArgumentException("String must contain only 0 and 1 characters");
                Bit bit = new Bit(c == '1');
                word.setBitN(j, bit);
            }
            
            // Write to memory at address i
            dram[i] = new Word32();
            word.copy(dram[i]);
        }
    }
}
