public class Word32 {
    public Word32() {
    }

    public Word32(Bit[] in) {
    }

    public void getTopHalf(Word16 result) { // sets result = bits 0-15 of this word. use bit.assign
    }

    public void getBottomHalf(Word16 result) { // sets result = bits 16-31 of this word. use bit.assign
    }

    public void copy(Word32 result) { // sets result's bit to be the same as this. use bit.assign
    }

    public boolean equals(Word32 other) {
        return false;
    }

    public static boolean equals(Word32 a, Word32 b) {
        return false;
    }

    public void getBitN(int n, Bit result) { // use bit.assign
    }

    public void setBitN(int n, Bit source) { //  use bit.assign
    }

    public void and(Word32 other, Word32 result) {
    }

    public static void and(Word32 a, Word32 b, Word32 result) {
    }

    public void or(Word32 other, Word32 result) {
    }

    public static void or(Word32 a, Word32 b, Word32 result) {
    }

    public void xor(Word32 other, Word32 result) {
    }

    public static void xor(Word32 a, Word32 b, Word32 result) {
    }

    public void not( Word32 result) {
    }

    public static void not(Word32 a, Word32 result) {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        /* -- match this format
        for (Bit bit : bits) {
            sb.append(bit.toString());
            sb.append(",");
        }
         */
        return sb.toString();
    }
}
