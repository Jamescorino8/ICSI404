public class ALU {
    public Word16 instruction = new Word16();
    public Word32 op1 = new Word32();
    public Word32 op2 = new Word32();
    public Word32 result = new Word32();
    public Bit less = new Bit(false);
    public Bit equal = new Bit(false);

    public void doInstruction() {
        // Reset and clear before operations
        Word32 zero = new Word32();
        zero.copy(result); 
        less.assign(false);
        equal.assign(false);
        
        // Get opcode from top 5 bits
        int opcode = 0;
        Bit b = new Bit(false);
        for (int i = 0; i < 5; i++) {
            instruction.getBitN(i, b);
            if (b.getValue()) {
                opcode += Math.pow(2, 4 - i);
            }
        }

        switch (opcode) {
            case 1: // ADD
                Adder.add(op1, op2, result);
                break;
            case 2: // AND
                Word32.and(op1, op2, result);
                break;
            case 3: // MULTIPLY
                Multiplier.multiply(op1, op2, result);
                break;
            case 4: // LEFT SHIFT
                Shifter.LeftShift(op1, getShiftAmount(), result);
                break;
            case 5: // SUBTRACT
                Adder.subtract(op1, op2, result);
                break;
            case 6: // OR
                Word32.or(op1, op2, result);
                break;
            case 7: // RIGHT SHIFT
                Shifter.RightShift(op1, getShiftAmount(), result);
                break;
            case 11: // COMPARE
                Word32 difference = new Word32();
                Adder.subtract(op1, op2, difference);

                // Check if op1 == op2 / all bits of difference are 0
                boolean allZero = true;
                b = new Bit(false);
                for (int i = 0; i < 32; i++) {
                    difference.getBitN(i, b);
                    if (b.getValue()) {
                        allZero = false;
                        break;
                    }
                }
                equal.assign(allZero);

                // Check if op1 < op2 / MSB of difference is 1
                difference.getBitN(0, b);
                less.assign(b.getValue());
                break;
            default:
                // default to ADD operation
                Adder.add(op1, op2, result);
                break;
        }
    }

    private int getShiftAmount() {
        // Only use lower 5 bits (bits 27-31) for shift amount
        int val = 0;
        Bit b = new Bit(false);
        for (int i = 27; i < 32; i++) {
            op2.getBitN(i, b);
            if (b.getValue()) {
                val += (int) Math.pow(2, 31 - i);
            }
        }
        return val;
    }
}