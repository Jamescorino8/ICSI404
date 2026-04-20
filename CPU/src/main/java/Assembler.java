import java.util.Map;

public class Assembler {
    private static final String ZERO = "00000";
    private static final Map<String, String> OPCODE_MAP = Map.ofEntries(
            Map.entry("halt", "00000"),
            Map.entry("add", "00001"),
            Map.entry("and", "00010"),
            Map.entry("multiply", "00011"),
            Map.entry("leftshift", "00100"),
            Map.entry("subtract", "00101"),
            Map.entry("or", "00110"),
            Map.entry("rightshift", "00111"),
            Map.entry("syscall", "01000"),
            Map.entry("call", "01001"),
            Map.entry("return", "01010"),
            Map.entry("compare", "01011"),
            Map.entry("ble", "01100"),
            Map.entry("blt", "01101"),
            Map.entry("bge", "01110"),
            Map.entry("bgt", "01111"),
            Map.entry("beq", "10000"),
            Map.entry("bne", "10001"),
            Map.entry("load", "10010"),
            Map.entry("store", "10011"),
            Map.entry("copy", "10100")
    );

    public static String[] assemble(String[] input) {
        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            String line = input[i].trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            String opcode = OPCODE_MAP.get(tokens[0]);

            String encoded;

            if (tokens.length == 1) {
                // No-argument instruction.
                encoded = opcode + "0" + ZERO + ZERO;

            } else if (tokens.length == 2) {
                // One-argument instruction.
                
                // Parse the immediate operand into Word32.
                int value = Integer.parseInt(tokens[1]);
                Word32 wordData = new Word32();
                TestConverter.fromInt(value, wordData);

                // Shift right by 5 to extract the upper operand field.
                Word32 shifted = new Word32();
                Shifter.RightShift(wordData, 5, shifted);

                // Build opA (upper 5 bits) and opB (lower 5 bits) from the immediate payload.
                StringBuilder opABuilder = new StringBuilder(5);
                StringBuilder opBBuilder = new StringBuilder(5);
                Bit bit = new Bit(false);
                for (int j = 27; j < 32; j++) {
                    shifted.getBitN(j, bit);
                    opABuilder.append(bit.getValue() ? '1' : '0');
                }
                for (int j = 27; j < 32; j++) {
                    wordData.getBitN(j, bit);
                    opBBuilder.append(bit.getValue() ? '1' : '0');
                }

                // build encoded output
                String opA = opABuilder.toString();
                String mode = String.valueOf(opA.charAt(0));
                encoded = opcode + mode + opA + opBBuilder;

            } else {
                // Two-argument instruction.
                String left = tokens[1];
                String right = tokens[2];

                int operandB = Integer.parseInt(right.substring(1));
                Word32 wordB = new Word32();
                TestConverter.fromInt(operandB, wordB);

                // Build the 5-bit opB field from the right side register op.
                StringBuilder opBBuilder = new StringBuilder(5);
                Bit bit = new Bit(false);
                for (int j = 27; j < 32; j++) {
                    wordB.getBitN(j, bit);
                    opBBuilder.append(bit.getValue() ? '1' : '0');
                }

                // Build opA from register or immediate and set mode (0=register, 1=immediate).
                if (!left.isEmpty() && left.charAt(0) == 'r') {
                    int operandA = Integer.parseInt(left.substring(1));
                    Word32 wordA = new Word32();
                    TestConverter.fromInt(operandA, wordA);
                    StringBuilder opABuilder = new StringBuilder(5);
                    for (int j = 27; j < 32; j++) {
                        wordA.getBitN(j, bit);
                        opABuilder.append(bit.getValue() ? '1' : '0');
                    }
                    encoded = opcode + "0" + opABuilder + opBBuilder;
                } else {
                    int immediate = Integer.parseInt(left);
                    Word32 wordA = new Word32();
                    TestConverter.fromInt(immediate, wordA);
                    StringBuilder opABuilder = new StringBuilder(5);
                    for (int j = 27; j < 32; j++) {
                        wordA.getBitN(j, bit);
                        opABuilder.append(bit.getValue() ? '1' : '0');
                    }
                    encoded = opcode + "1" + opABuilder + opBBuilder;
                }
            }

            output[i] = encoded;
        }
        return output;
    }

    public static String[] finalOutput(String[] input) {
        String[] lines = input;
        // Pad with a halt so we can always merge into 32-bit lines.
        if (lines.length % 2 != 0) {
            String[] withHalt = new String[lines.length + 1];
            for (int i = 0; i < lines.length; i++) {
                withHalt[i] = lines[i];
            }
            withHalt[withHalt.length - 1] = "0000000000000000";
            lines = withHalt;
        }

        // Merge lines
        String[] merged = new String[lines.length - 1];
        for (int i = 0; i < lines.length - 1; i++) {
            merged[i] = lines[i] + lines[i + 1];
        }
        return merged;
    }
}
