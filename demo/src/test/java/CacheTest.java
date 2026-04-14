import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CacheTest {
    // Sum integers 1-100 via count-down. Result in r0.
    @Test
    public void testSum() {
        String[] program = {
            "copy 0 r0",        // sum = 0
            "copy 10 r1",       // r1 = 10
            "multiply 10 r1",   // r1 = 100, countdown counter
            "add r1 r0",        // sum += counter
            "subtract 1 r1",    // counter -= 1
            "compare 0 r1",     // compare 0 to counter
            "bne -1",           // if counter != 0, loop to PC = 3
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
            "copy 10 r2",      // r2 = 10
            "multiply 10 r2",  // r2 = 100, count
            "copy r2 r1",      // r1 = 100
            "add r1 r1",       // r1 = 200, start address
            "copy r1 r6",      // r6 = 200, save start address for reset after fill
            "copy r2 r5",      // r5 = 100
            "multiply 3 r5",   // r5 = 300, end address
            "copy 1 r0",       // r0 = 1, first value to store
            // Fill
            "store r0 r1",     // store current value to current address, PC = 8
            "add 1 r0",        // value to store += 1
            "add 1 r1",        // address += 1
            "subtract 1 r2",   // count -= 1
            "compare 0 r2",    // compare 0 to count
            "bne -2",          // if count != 0, loop to PC = 8
            // Sum
            "copy r6 r1",      // reset address to start address (200)
            "copy 0 r3",       // r3 = 0, sum
            "copy r1 r4",      // copy current address to r1, PC = 16
            "load 0 r4",       // r4 = current address
            "add r4 r3",       // sum += value
            "add 1 r1",        // address += 1
            "compare r5 r1",   // compare end address to current address
            "blt -2",          // if address < 300, loop to PC = 16
            "syscall 0",
            "halt"
        };
        var p = runProgram(program);
        // r3 = 5050 = 0x13BA
        assertEquals("r3:0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,1,1,1,0,1,0,",
            p.output.get(3));
        System.out.println("[testSumArray] clock cycles: " + p.currentClockCycle);
    }

    // Build a linked list of 100 nodes at addresses 200-399.
    @Test
    public void testSumLinkedList() {
        String[] program = {
            "copy 10 r2",      // r2 = 10
            "multiply 10 r2",  // r2 = 100
            "copy r2 r1",      // r1 = 100
            "add r1 r1",       // r1 = 200, address of head node
            "copy r1 r6",      // r6 = 200, save head node for traversal
            "subtract 1 r2",   // r2 = 99, build loop count
            "copy 1 r0",       // r0 = 1, value to store
            // Build 99 nodes with next pointers
            "store r0 r1",     // current address = current value, PC = 7
            "copy r1 r3",      // save current node address before incremented into r3
            "add 2 r3",        // r3 = address of next node (+2 ahead)
            "add 1 r1",        // r1 = address of next pointer slot
            "store r3 r1",     // store next node's address to pointer slot
            "add 1 r1",        // r1 += 1, start of next node
            "add 1 r0",        // current value += 1
            "subtract 1 r2",   // count -= 1
            "compare 0 r2",    // compare 0 to count
            "bne -4",          // if count != 0, loop to PC=7
            // Last node
            "store r0 r1",     // store final value
            "add 1 r1",        // r1 += 1, pointer slot of last node
            "store 0 r1",      // write null terminator using immediate mode
            // Traverse and sum
            "copy r6 r1",      // reset r1 to head address
            "copy 0 r3",       // sum = 0
            "copy r1 r4",      // copy current address to r4, PC = 22
            "load 0 r4",       // r4 = node value
            "add r4 r3",       // sum += value
            "add 1 r1",        // r1 = current address + 1, next pointer
            "load 0 r1",       // load next node address
            "compare 0 r1",    // compare 0 to next node address
            "copy r3 r3",      // NOP, shifts bne to PC = 29 (odd) to land exactly on PC = 22 (even)
            "bne -3",          // if next node address != 0, loop to PC = 22
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
