public class Adder {
    public static void subtract(Word32 a, Word32 b, Word32 result) {
        // two's complement: a - b = a + (-b + 1)
        Word32 notB = new Word32();
        b.not(notB);
        
        // create a Word32 representing 1
        Word32 one = new Word32();
        one.setBitN(31, new Bit(true)); // Set LSB to 1
        
        Word32 temp = new Word32();
        add(notB, one, temp);
        
        add(a, temp, result);
    }

    public static void add(Word32 a, Word32 b, Word32 result) {   
        Bit carry = new Bit(false);
        
        // from LSB to MSB
        for (int i = 31; i >= 0; i--) {
            Bit bitA = new Bit(false);
            Bit bitB = new Bit(false);
            a.getBitN(i, bitA);
            b.getBitN(i, bitB);

            Bit temp = new Bit(false);
            Bit temp2 = new Bit(false);
            
            // bitA XOR bitB XOR carry
            Bit.xor(bitA, bitB, temp);
            Bit.xor(temp, carry, temp2);
            result.setBitN(i, temp2);
            
            // (bitA AND bitB) OR ((bitA XOR bitB) AND carry)
            Bit.and(bitA, bitB, temp);
            Bit.xor(bitA, bitB, temp2);
            Bit.and(temp2, carry, temp2);
            Bit.or(temp, temp2, carry);
        }
    }
}
