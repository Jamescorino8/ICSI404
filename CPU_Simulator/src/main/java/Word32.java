public class Word32 {
    private Bit[] word;

    public Word32() {
        word = new Bit[32];
        for (int i = 0; i < 32; i++) {
            // Initialize bits within array
            word[i] = new Bit(false);
        }
    }

    public Word32(Bit[] in) {
        word = in;
    }

    public void getTopHalf(Word16 result) { // sets result = bits 0-15 of this word. use bit.assign
        for (int i = 0; i < 16 ; i++) {
            result.setBitN(i, word[i]);
        }
    }

    public void getBottomHalf(Word16 result) { // sets result = bits 16-31 of this word. use bit.assign
        for (int i = 16; i < 32 ; i++) {
            result.setBitN(i - 16, word[i]);
        }
    }

    public void copy(Word32 result) { // sets result's bit to be the same as this. use bit.assign
        int i = 0;    
        for (Bit bit : word) {
            result.setBitN(i++, bit);
        }
    }

    public void getBitN(int n, Bit result) { // use bit.assign
        result.assign(word[n].getValue());
    }

    public void setBitN(int n, Bit source) { //  use bit.assign
        word[n].assign(source.getValue());
    }

    public boolean equals(Word32 other) {
        return equals(this, other);
    }

    public static boolean equals(Word32 a, Word32 b) {
        boolean isEqual = true;
        for (int i = 0; i < 32 ; i++) {
            if (a.word[i].getValue() != b.word[i].getValue()) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    public void and(Word32 other, Word32 result) {
        and(this, other, result);
    }

    public static void and(Word32 a, Word32 b, Word32 result) {
        for (int i = 0; i < 32 ; i++) {
            Bit.and(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void or(Word32 other, Word32 result) {
        or(this, other, result);
    }

    public static void or(Word32 a, Word32 b, Word32 result) {
        for (int i = 0; i < 32 ; i++) {
            Bit.or(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void xor(Word32 other, Word32 result) {
        xor(this, other, result);
    }

    public static void xor(Word32 a, Word32 b, Word32 result) {
        for (int i = 0; i < 32 ; i++) {
            Bit.xor(a.word[i], b.word[i], result.word[i]);
        }
    }

    public void not( Word32 result) {
        not(this, result);
    }

    public static void not(Word32 a, Word32 result) {
        for (int i = 0; i < 32 ; i++) {
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
