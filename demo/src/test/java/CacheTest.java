import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

// NOTE: The 2-argument immediate field is only 5 bits (values 0-15 positive).
// Large constants must be built with multiply/shift. For example:
//   100 = 10 * 10  -> copy 10 rX, multiply 10 rX
//   200 = 100 * 2  -> add rX rX  (or leftshift 1 rX)
//   300 = 100 * 3  -> multiply 3 rX

public class CacheTest {
    // Sum integers 1-100 via count-down. Result in r0.
    @Test
    public void testSum() {
        String[] program = {
            "copy 0 r0",        // sum = 0
            "copy 10 r1",       // r1 = 10
            "multiply 10 r1",   // r1 = 100
            "add r1 r0",        // sum += counter
            "subtract 1 r1",    // counter--
            "compare 0 r1",     // compare 0 to counter
            "bne -1",           // if counter!=0, loop to PC=3
            "syscall 0",
            "halt"
        };
        var p = runProgram(program);
        // r0 = 5050 = 0x13BA
        assertEquals("r0:0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,1,1,1,0,1,0,", p.output.getFirst());
        System.out.println("[testSum] clock cycles: " + p.currentClockCycle);
    }

    // Fill addresses 200-299 with values 1-100, then sum.
    @Test
    public void testSumArray() {
        String[] program = {
            // Init
            "copy 10 r2",      // r2 = 10
            "multiply 10 r2",  // r2 = 100
            "copy r2 r1",      // r1 = 100
            "add r1 r1",       // r1 = 200 (start address)
            "copy r1 r6",      // r6 = 200 (save start for reset after fill)
            "copy r2 r5",      // r5 = 100
            "multiply 3 r5",   // r5 = 300 (end address)
            "copy 1 r0",       // value = 1
            // Fill phase
            "store r0 r1",     // [FILL LOOP] mem[addr] = value
            "add 1 r0",        // value++
            "add 1 r1",        // addr++
            "subtract 1 r2",   // count--
            "compare 0 r2",    // compare 0 to count
            "bne -2",          // if count!=0, loop to PC=8
            // Sum phase
            "copy r6 r1",      // r1 = 200 (restore start addr)
            "copy 0 r3",       // sum = 0
            "copy r1 r4",      // [SUM LOOP] r4 = addr (copy so load doesn't destroy r1)
            "load 0 r4",       // r4 = mem[addr]
            "add r4 r3",       // sum += value
            "add 1 r1",        // addr++
            "compare r5 r1",   // compare end(src=300) to addr(dst=r1)
            "blt -2",          // if addr<300, loop to PC=16
            "syscall 0",
            "halt"
        };
        var p = runProgram(program);
        // r3 = 5050 = 0x13BA
        assertEquals("r3:0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,1,1,1,0,1,0,",
            p.output.get(3));
        System.out.println("[testSumArray] clock cycles: " + p.currentClockCycle);
    }

    // ---------------------------------------------------------------
    // Program 3: Build a linked list of 100 nodes at addresses 200-399.
    // Each node: mem[addr]=value, mem[addr+1]=next_addr (0 for last node).
    // Traverse and sum all values. Result in r3.
    //
    // --- Init (PC 0-6) ---
    // PC=0  copy 10 r2       r2 = 10
    // PC=1  multiply 10 r2   r2 = 100
    // PC=2  copy r2 r1       r1 = 100
    // PC=3  add r1 r1        r1 = 200 (start / head address)
    // PC=4  copy r1 r6       r6 = 200 (save head for traversal reset)
    // PC=5  subtract 1 r2    r2 = 99 (build count: last node handled separately)
    // PC=6  copy 1 r0        value = 1
    //
    // --- Build 99 nodes with valid next pointers (PC 7-16) ---
    // PC=7  store r0 r1      [BUILD LOOP, odd PC] mem[addr] = value
    // PC=8  copy r1 r3       r3 = addr
    // PC=9  add 2 r3         r3 = addr+2 (next node address)
    // PC=10 add 1 r1         r1 = addr+1 (next pointer slot)
    // PC=11 store r3 r1      mem[addr+1] = next node address
    // PC=12 add 1 r1         r1 = addr+2 (advance to next node)
    // PC=13 add 1 r0         value++
    // PC=14 subtract 1 r2    count--
    // PC=15 compare 0 r2     op1=r2(count), op2=0
    // PC=16 bne -4           if count!=0, loop: 16+2(-4)-1=7 (even->odd) ✓
    //
    // --- Last node at addr=398, value=100 (PC 17-19) ---
    // PC=17 store r0 r1      mem[398] = 100
    // PC=18 add 1 r1         r1 = 399 (next pointer slot)
    // PC=19 store 0 r1       mem[399] = 0 (null terminator, immediate mode)
    //
    // --- Traverse and sum (PC 20-31) ---
    // PC=20 copy r6 r1       r1 = 200 (head)
    // PC=21 copy 0 r3        sum = 0
    // PC=22 copy r1 r4       [SUM LOOP, even PC] r4 = node addr
    // PC=23 load 0 r4        r4 = value at node  (r1 unchanged)
    // PC=24 add r4 r3        sum += value
    // PC=25 add 1 r1         r1 = addr+1 (next pointer slot)
    // PC=26 load 0 r1        r1 = next node address (follows the pointer)
    // PC=27 compare 0 r1     op1=r1(next), op2=0: equal if null
    // PC=28 copy r3 r3       nop (parity pad: makes branch land on even PC=22)
    // PC=29 bne -3           if next!=0, loop: 29+2(-3)-1=22 (odd->even) ✓
    // PC=30 syscall 0
    // PC=31 halt
    // ---------------------------------------------------------------
    @Test
    public void testSumLinkedList() {
        String[] program = {
            // Init
            "copy 10 r2",      // r2 = 10
            "multiply 10 r2",  // r2 = 100
            "copy r2 r1",      // r1 = 100
            "add r1 r1",       // r1 = 200 (head address)
            "copy r1 r6",      // r6 = 200 (save head for traversal)
            "subtract 1 r2",   // r2 = 99 (build loop count)
            "copy 1 r0",       // value = 1
            // Build 99 nodes with next pointers
            "store r0 r1",     // [BUILD LOOP] mem[addr] = value
            "copy r1 r3",      // r3 = addr
            "add 2 r3",        // r3 = addr+2 (next node)
            "add 1 r1",        // r1 = addr+1 (next ptr slot)
            "store r3 r1",     // mem[addr+1] = next node addr
            "add 1 r1",        // r1 = addr+2 (next node)
            "add 1 r0",        // value++
            "subtract 1 r2",   // count--
            "compare 0 r2",    // compare 0 to count
            "bne -4",          // if count!=0, loop to PC=7
            // Last node (addr=398, value=100)
            "store r0 r1",     // mem[398] = 100
            "add 1 r1",        // r1 = 399 (next ptr slot)
            "store 0 r1",      // mem[399] = 0 (null, immediate mode)
            // Traverse and sum
            "copy r6 r1",      // r1 = 200 (head)
            "copy 0 r3",       // sum = 0
            "copy r1 r4",      // [SUM LOOP] r4 = node addr (copy before load destroys r4)
            "load 0 r4",       // r4 = node value
            "add r4 r3",       // sum += value
            "add 1 r1",        // r1 = addr+1 (next ptr slot)
            "load 0 r1",       // r1 = next node addr (follows pointer)
            "compare 0 r1",    // compare 0 to next (equal if null)
            "copy r3 r3",      // nop (parity padding)
            "bne -3",          // if next!=0, loop to PC=22
            "syscall 0",
            "halt"
        };
        var p = runProgram(program);
        // r3 = 5050 = 0x13BA
        assertEquals("r3:0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,1,1,1,0,1,0,",
            p.output.get(3));
        System.out.println("[testSumLinkedList] clock cycles: " + p.currentClockCycle);
    }

    private static Processor runProgram(String[] program) {
        // load assemnled program into memory and run through processor; return output
        var assembled = Assembler.assemble(program);
        var merged = Assembler.finalOutput(assembled);
        var m = new Memory();
        m.load(merged); 
        var p = new Processor(m);
        p.run();
        return p;
    }
}
