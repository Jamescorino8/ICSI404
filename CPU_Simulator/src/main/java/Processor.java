import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Processor {
    private Memory mem;
    public List<String> output = new LinkedList<>();
    int programCounter = 0; // PC
    boolean pcUpdated = false; // True if execute updated PC
    Word16 instructionRegister;
    int opcode, mode, opA, opB;
    Word32[] registers = new Word32[32];
    Word32 op1 = new Word32(); // decoded operand 1
    Word32 op2 = new Word32(); // decoded operand 2
    Word32 result = new Word32(); // result from execute
    ALU alu = new ALU();
    Stack<Integer> callStack = new Stack<>(); // Stack of return addresses for call/return instructions
    int currentClockCycle = 0;
    InstructionCache instructionCache;
    L2Cache l2;

    public Processor(Memory m) {
        mem = m;
        l2 = new L2Cache(m);
        instructionCache = new InstructionCache(l2);
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
        // Clear value so null entries read as zero (halt)
        new Word32().copy(instructionCache.value);

        Word32 addr = new Word32();
        TestConverter.fromInt(programCounter, addr);
        addr.copy(instructionCache.address);
        instructionCache.read();
        currentClockCycle += instructionCache.lastCost;

        // Figure out which half holds current instruction
        Bit b = new Bit(false);
        boolean topHalfIsZero = true;
        for (int i = 0; i < 16; i++) {
            instructionCache.value.getBitN(i, b);
            if (b.getValue()) { 
                topHalfIsZero = false; 
                break; 
            }
        }

        if (topHalfIsZero && programCounter > 0) {
            new Word32().copy(instructionCache.value);
            TestConverter.fromInt(programCounter - 1, addr);
            instructionCache.address = addr;
            instructionCache.read();
            instructionCache.value.getBottomHalf(instructionRegister);
        } else {
            instructionCache.value.getTopHalf(instructionRegister);
        }
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
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 11: case 18: case 20:
                // 2 arg
                registers[opB].copy(op1);
                if (mode == 0) {
                    registers[opA].copy(op2);
                } else {
                    int imm5 = (opA & 0x10) != 0 ? opA - 32 : opA; // 5 bit signed immediate value
                    TestConverter.fromInt(imm5, op2);
                }
                break;
            case 19:
                // store
                if (mode == 0) {
                    registers[opA].copy(op1);
                } else {
                    int imm5 = (opA & 0x10) != 0 ? opA - 32 : opA; // 5 bit signed immediate value
                    TestConverter.fromInt(imm5, op1);
                }
                registers[opB].copy(op2);
                break;
            case 9: case 12: case 13: case 14: case 15: case 16: case 17:
                // 1 arg 
                int raw = (opA << 5) | opB;
                int imm10 = (raw & 0x200) != 0 ? raw - 1024 : raw; // 10 bit signed immediate value
                TestConverter.fromInt(imm10, op1);
                break;
        }
    }

    private void execute() {
        pcUpdated = false;

        switch(opcode) {
            case 0:
                // Halt
                output.add("halt");
                System.out.println(currentClockCycle);
                break;
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 11:
                // ALU ops (add, subtract, multiply, shifts, bitwise, compare)
                instructionRegister.copy(alu.instruction);
                op1.copy(alu.op1);
                op2.copy(alu.op2);
                alu.doInstruction();
                alu.result.copy(result);
                currentClockCycle += opcode == 3 ? 10 : 2;
                break;
            case 8:
                // Syscall
                if (opB == 0) {
                    printReg();
                } else if (opB == 1) {
                    printMem();
                }
                break;
            case 9:
                // Call: Pushes the current address + 1 on the stack. Sets PC to PC + Immediate
                callStack.push(programCounter + 1);
                programCounter = programCounter + 2 * TestConverter.toInt(op1) - 1;
                pcUpdated = true;
                break;
            case 10:
                // Return: Pops from the stack and sets the PC to the popped value
                programCounter = callStack.pop();
                pcUpdated = true;
                break;
            case 12:
                // BLE
                if (alu.less.getValue() || alu.equal.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 13:
                // BLT
                if (alu.less.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 14:
                // BGE
                if (!alu.less.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 15:
                // BGT
                if (!alu.less.getValue() && !alu.equal.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 16:
                // BEQ
                if (alu.equal.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 17:
                // BNE
                if (!alu.equal.getValue()) {
                    programCounter += 2 * TestConverter.toInt(op1) - 1;
                    pcUpdated = true;
                }
                break;
            case 18:
                // Load
                Word32 loadAddr = new Word32();
                Adder.add(op1, op2, loadAddr);
                loadAddr.copy(l2.address);
                l2.read();
                l2.value.copy(result);
                currentClockCycle += l2.lastCost;
                break;
            case 19:
                // Store
                op2.copy(l2.address);
                op1.copy(l2.value);
                l2.write();
                currentClockCycle += l2.lastCost;
                break;
            case 20:
                // Copy
                op2.copy(result);
                break;
        }
    }

    private void printReg() {
        for (int i = 0; i < 32; i++) {
            var line = "r" + i + ":" + registers[i].toString();
            output.add(line);
            System.out.println(line);
        }
    }

    private void printMem() {
        for (int i = 0; i < 1000; i++) {
            Word32 addr = new Word32();
            TestConverter.fromInt(i, addr);
            addr.copy(mem.address);
            mem.read();
            Word32 value = new Word32();
            mem.value.copy(value);
            var line = i + ":" + value.toString() + "(" + TestConverter.toInt(value) + ")";
            output.add(line);
            System.out.println(line);
        }
    }

    private void store() {
        // Write result to registers[opB]
        switch (opcode) {
            // opcodes that produce a new value
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 18: case 20: 
                result.copy(registers[opB]);
                break;
        }
        if (!pcUpdated) {
            programCounter++;
        }
    }
}
