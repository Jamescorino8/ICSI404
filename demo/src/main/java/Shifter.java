public class Shifter {
    public static void LeftShift(Word32 source, int amount, Word32 result) {
        /**
         * Example (rshift by 2):
         *  source: 0000 0000 0000 0001 0001 
         *  result: 0000 0000 0000 0100 0100 (lshift by 2)
         */
        Bit currentBit = new Bit(false);
        // i = source pointer, j = result pointer
        for (int i = 31, j = 31 - amount; i >= 0 && j >= 0; i--, j--) {
            source.getBitN(i, currentBit);
            result.setBitN(j, currentBit);
        }
    }

    public static void RightShift(Word32 source, int amount, Word32 result) {
        /**
         * Example (rshift by 2):
         *  source: 0000 0000 0000 0001 0001 
         *  result: 0000 0000 0000 0000 0100 
         */
        Bit currentBit = new Bit(false);
        // i = source pointer, j = result pointer
        for (int i = 31 - amount, j = 31; i >= 0 && j >= 0; i--, j--) {
            source.getBitN(i, currentBit);
            result.setBitN(j, currentBit);
        }
    }
}
