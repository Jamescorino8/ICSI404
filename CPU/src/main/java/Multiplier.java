public class Multiplier {
    public static void multiply(Word32 a, Word32 b, Word32 result) {
        Bit bitA = new Bit(false);
        Bit bitB = new Bit(false);
        Bit tempBit = new Bit(false);

        Word32 tempWord32 = new Word32();

        /**
         * Two pointers
         * int i: from LSB to MSB of Word32 b
         * int j: from LSB to MSB of word32 a, while bitB is unchanged
         */
        for (int i = 31; i >= 0; i--) {
            b.getBitN(i, bitB);
            tempWord32 = new Word32();
            for (int j = 31; j >= 0; j--) {
                a.getBitN(j, bitA);
                // bitA AND bitB = result bit shifted by (31-i) positions
                Bit.and(bitA, bitB, tempBit);
                // Shift a left by (31-i) positions
                int targetPosition = j - (31 - i);
                if (targetPosition >= 0) {
                    tempWord32.setBitN(targetPosition, tempBit);
                }
            }
            // add tempWord32 to result
            Adder.add(result, tempWord32, result);
        }
    }
}
