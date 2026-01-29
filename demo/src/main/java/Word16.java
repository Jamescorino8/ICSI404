public class Word16 {
    private Bit[] word;

    public Word16() {
        word = new Bit[16];
        for (int i = 0; i < 16; i++) {
            // Initialize bits within array
            word[i] = new Bit(false);
        }
    }

    public Word16(Bit[] in) {
        word = in;
    }

    public void copy(Word16 result) { // sets the values in "result" to be the same as the values in this instance; use "bit.assign"
        int i = 0;    
        for (Bit bit : word) {
                result.setBitN(i++, bit);
        }
    }

    public void getBitN(int n, Bit result) { // sets result to be the same value as the nth bit of this word
        result.assign(word[n].getValue());
    }

    public void setBitN(int n, Bit source) { // sets the nth bit of this word to "source"
        word[n].assign(source.getValue());
    }

    public boolean equals(Word16 other) { // is other equal to this
        return equals(this, other);
    }

    public static boolean equals(Word16 a, Word16 b) {
        boolean isEqual = true;
        for (int i = 0; i < 16 ; i++) {
            if (a.word[i].getValue() != b.word[i].getValue()) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    public void and(Word16 other, Word16 result) {
        and(this, other, result);
    }

    public static void and(Word16 a, Word16 b, Word16 result) {
        for (int i = 0; i < 16 ; i++) {
            Bit.and(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void or(Word16 other, Word16 result) {
        or(this, other, result);
    }

    public static void or(Word16 a, Word16 b, Word16 result) {
        for (int i = 0; i < 16 ; i++) {
            Bit.or(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void xor(Word16 other, Word16 result) {
        xor(this, other, result);
    }

    public static void xor(Word16 a, Word16 b, Word16 result) {
        for (int i = 0; i < 16 ; i++) {
            Bit.xor(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void not(Word16 result) {
        not(this, result);
    }

    public static void not(Word16 a, Word16 result) {
        for (int i = 0; i < 16 ; i++) {
            Bit.not(a.word[i], result.word[i]);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : word) {
            sb.append(bit.toString());
            sb.append(",");
        }
        return sb.toString();
    }
}