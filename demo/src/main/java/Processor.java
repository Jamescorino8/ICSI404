import java.util.LinkedList;
import java.util.List;

public class Processor {
    private Memory mem;
    public List<String> output = new LinkedList<>();
    int programCounter = 0;
    Word16 instructionRegister;
    int opcode, mode, opA, opB;

    public Processor(Memory m) {
        mem = m;
        instructionRegister = new Word16();
    }

    public void run() {
        while (!output.contains("halt")) {
            fetch();
            decode();
            execute();
            store();
        }
    }

    // read instruction from memory
    private void fetch() {
        Word32 addr = new Word32();
        TestConverter.fromInt(programCounter, addr);
        mem.address = addr;

        mem.read();
        mem.value.getTopHalf(instructionRegister);
    }

    private void decode() {
        StringBuilder opcodeBits = new StringBuilder(5);
        String modeBit = "0";
        StringBuilder opABits = new StringBuilder(5);
        StringBuilder opBBits = new StringBuilder(5);
        Bit temp = new Bit(false);
        
        for (int i = 0; i < 16; i++) {
            instructionRegister.getBitN(i, temp);
            if (i < 5) {
                opcodeBits.append(temp.getValue() ? "1" : "0");
            } else if (i == 5) {
                modeBit = temp.getValue() ? "1" : "0";
            } else if (i > 5 && i < 11) {
                opABits.append(temp.getValue() ? "1" : "0");
            } else {
                opBBits.append(temp.getValue() ? "1" : "0");
            }
        }

        this.opcode = Integer.parseInt(opcodeBits.toString(), 2);
        this.mode = Integer.parseInt(modeBit, 2);
        this.opA = Integer.parseInt(opABits.toString(), 2);
        this.opB = Integer.parseInt(opBBits.toString(), 2);

    }

    private void execute() {
    }

    private void printReg() {
        for (int i = 0; i < 32; i++) {
            var line = "r"+ i + ":" + ""; // TODO: add the register value here...
            output.add(line);
            System.out.println(line);
        }
    }

    private void printMem() {
        for (int i = 0; i < 1000; i++) {
            Word32 addr = new Word32();
            Word32 value = new Word32();
            // Convert i to Word32 here...
            addr.copy(mem.address);
            mem.read();
            mem.value.copy(value);
            var line = i + ":" + value + "(" + TestConverter.toInt(value) + ")";
            output.add(line);
            System.out.println(line);
        }
    }

    private void store() {
    }
}