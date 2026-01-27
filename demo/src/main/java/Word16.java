public class Word16 {

    public Word16() {
    }

    public Word16(Bit[] in) {}

    public void copy(Word16 result) { // sets the values in "result" to be the same as the values in this instance; use "bit.assign"
    }

    public void setBitN(int n, Bit source) { // sets the nth bit of this word to "source"
    }

    public void getBitN(int n, Bit result) { // sets result to be the same value as the nth bit of this word
    }

    public boolean equals(Word16 other) { // is other equal to this
        return false;
    }

    public static boolean equals(Word16 a, Word16 b) {
        return false;
    }

    public void and(Word16 other, Word16 result) {
    }

    public static void and(Word16 a, Word16 b, Word16 result) {
    }

    public void or(Word16 other, Word16 result) {
    }

    public static void or(Word16 a, Word16 b, Word16 result) {
    }

    public void xor(Word16 other, Word16 result) {
    }

    public static void xor(Word16 a, Word16 b, Word16 result) {
    }

    public void not( Word16 result) {
    }

    public static void not(Word16 a, Word16 result) {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        /*  -- match this format
        for (Bit bit : bits) {
            sb.append(bit.toString());
            sb.append(",");
        }
         */
        return sb.toString();
    }
}