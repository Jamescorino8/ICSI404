public class Bit {
    private boolean value;

    public Bit(boolean value) {
        assign(value);
    }

    public boolean getValue() {
        return this.value;
    }

    public void assign(boolean value) {
        this.value = value;
    }

    public void and(Bit b2, Bit result) {
        and(this, b2, result);
        
    }

    public static void and(Bit b1, Bit b2, Bit result) {
        result.assign(b1.getValue() == false ? false : (b2.getValue() == false ? false : true));
    }

    public void or(Bit b2, Bit result) {
        or(this, b2, result);
    }

    public static void or(Bit b1, Bit b2, Bit result) {
        result.assign(b1.getValue() == false ? (b2.getValue() == false ? false : true) : true);
    }

    public void xor(Bit b2, Bit result) {
        xor(this, b2, result);
    }

    public static void xor(Bit b1, Bit b2, Bit result) {
        result.assign(b1.getValue() == false ? (b2.getValue() == false ? false : true) : (b2.getValue() == false ? true : false));
    }

    public void not(Bit result) {
        not(this, result);
    }

    public static void not(Bit b1, Bit result) {
        result.assign(b1.getValue() == false ? true : false);
    }

    public String toString() {
        return value == true ? "1" : "0";
    }
}
