import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Processor {
    private Memory mem;
    public List<String> output = new LinkedList<>();
    int programCounter = 0;
    Word16 instructionRegister;
    int opcode, mode, opA, opB;
    Word32[] registers = new Word32[32];
    Word32 op1 = new Word32(); // decoded operand 1
    Word32 op2 = new Word32(); // decoded operand 2
    Word32 result = new Word32(); // result from execute
    ALU alu = new ALU();
    Stack<Integer> callStack = new Stack<>();
    boolean pcUpdated = false; // True if execute updated the PC

    public Processor(Memory m) {
        mem = m;
        instructionRegister = new Word16();
        // initialize registers
        for (int i = 0; i < 32; i++) {
            registers[i] = new Word32();
        }
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

        // fill op1 and op2
        switch(opcode) {
            case 0: case 8: case 10:
                break; 
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 11:
                // 2 arg
                if (mode == 0) {
                    registers[opA].copy(op1);
                } else {
                    // sign extend 5bit immediate. negative if bit 4 set
                    int imm5 = (opA & 0x10) != 0 ? opA-32 : opA;
                    TestConverter.fromInt(imm5, op1);
                }
                registers[opB].copy(op2);
                break;
            case 9: case 12: case 13: case 14: case 15: case 16: case 17:
                // 1 arg
                int raw = (opA << 5) | opB;
                int imm10 = (raw & 0x200) != 0 ? raw - 1024 : raw;
                TestConverter.fromInt(imm10, op1);
                break;
            case 18:
                // load
                registers[opA].copy(op1);
                break;
            case 19:
                // store
                registers[opA].copy(op1);
                registers[opB].copy(op2);
                break;
        }
    }

    private void execute() {
        pcUpdated = false;

        switch(opcode) {
            case 0:
                // halt
                output.add("halt");
                break;
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 11:
                // ALU ops
                instructionRegister.copy(alu.instruction);
                op1.copy(alu.op1);
                op2.copy(alu.op2);
                alu.doInstruction();
                alu.result.copy(result);
                break;
            case 8:
                // syscall
                String val = String.valueOf(TestConverter.toInt(registers[0]));
                output.add(val);
                System.out.println(val);
                break;
            case 9:
                // call
                callStack.push(programCounter + 1);
                programCounter = TestConverter.toInt(op1);
                pcUpdated = true;
                break;
            case 10:
                // return
                programCounter = callStack.pop();
                pcUpdated = true;
                break;
            case 12:
                // ble
                if (alu.less.getValue() || alu.equal.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 13:
                // blt
                if (alu.less.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 14:
                // bge
                if (!alu.less.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 15:
                // bgt
                if (!alu.less.getValue() && !alu.equal.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 16:
                // beq
                if (alu.equal.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 17:
                // bne
                if (!alu.equal.getValue()) {
                    programCounter += TestConverter.toInt(op1);
                    pcUpdated = true;
                }
                break;
            case 18:
                // load
                op1.copy(mem.address);
                mem.read();
                mem.value.copy(result);
                break;
            case 19:
                // store
                op2.copy(mem.address);
                op1.copy(mem.value);
                mem.write();
                break;
            case 20:
                // copy
                op1.copy(result);
                break;
        }
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