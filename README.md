# CPU Simulator

A working CPU in Java, built from a class that represents a single bit.

Everything above sits on that foundation: 16- and 32-bit words, a ripple-carry adder, a multiplier, a shifter, then an ALU dispatching across them. Above that, a 1000-word simulated DRAM, a two-level cache hierarchy, an assembler, and a processor running fetch-decode-execute-store over a 21-instruction ISA.

No layer uses a Java primitive where the hardware it models would use something more fundamental. By the time the processor executes `add r1 r0`, the addition has propagated through gate-level logic rather than reaching for `+`.

## Layers

| Layer | Classes |
|---|---|
| Bits and words | `Bit`, `Word16`, `Word32` |
| Arithmetic | `Adder` (ripple-carry), `Multiplier`, `Shifter` |
| Execution | `ALU`, `Processor` |
| Memory | `Memory` (1000 words), `InstructionCache` (L1), `L2Cache` |
| Tooling | `Assembler`, `TestConverter` |

## Cache performance

The final stage was measurement rather than construction. Three programs with deliberately different memory access patterns, each run under four cache configurations, counting cycles.

| Program | No cache | L1 instruction | L1 + L2 (instr) | L1 + L2 (instr + data) |
|---|---|---|---|---|
| Sum 1 to 100 (registers) | 61,510 | 2,980 | 3,000 | 3,000 |
| Sum array (memory) | 243,222 | 68,162 | 68,202 | 50,622 |
| Sum linked list (memory) | 392,704 | 198,494 | 135,834 | 100,334 |

The register-only loop improves 20x from the instruction cache alone, since it touches memory only to fetch instructions. The array program gains another 26% once L2 caches data as well as instructions. The linked list, whose pointer chasing defeats prefetching of any kind, needs the full hierarchy to reach 3.9x and still finishes slowest. The gains track how sequential the access pattern is.

The three benchmark programs are the tests in `CacheTest`, each printing its own cycle count. Full writeup in [`CacheReport.pdf`](CacheReport.pdf).

## Instruction set

Instructions are 16 bits wide, two packed into each 32-bit memory word.

| Field  | Bits | Description                         |
|--------|------|-------------------------------------|
| opcode | 5    | Operation                           |
| mode   | 1    | 0 = register operand, 1 = immediate |
| opA    | 5    | Source register or immediate value  |
| opB    | 5    | Destination register                |

| Opcode | Mnemonic   | Description                            |
|--------|------------|----------------------------------------|
| 0      | halt       | Stop execution                         |
| 1      | add        | opB = opB + opA                        |
| 2      | and        | opB = opB & opA (bitwise)              |
| 3      | multiply   | opB = opB * opA                        |
| 4      | leftshift  | opB = opB << opA                       |
| 5      | subtract   | opB = opB - opA                        |
| 6      | or         | opB = opB \| opA (bitwise)             |
| 7      | rightshift | opB = opB >> opA                       |
| 8      | syscall    | 0 = print registers, 1 = print memory  |
| 9      | call       | Push PC+1, jump to PC + immediate      |
| 10     | return     | Pop and jump to return address         |
| 11     | compare    | Set less/equal flags (opB vs opA)      |
| 12     | ble        | Branch if less-or-equal                |
| 13     | blt        | Branch if less-than                    |
| 14     | bge        | Branch if greater-or-equal             |
| 15     | bgt        | Branch if greater-than                 |
| 16     | beq        | Branch if equal                        |
| 17     | bne        | Branch if not-equal                    |
| 18     | load       | opB = Memory[opB + opA]                |
| 19     | store      | Memory[opB] = opA                      |
| 20     | copy       | opB = opA                              |

## Cache hierarchy

The processor supports four cache modes, set via the `cacheMode` field.

| Mode | Configuration                 | IC miss cost | L2 miss cost | Memory cost |
|------|-------------------------------|--------------|--------------|-------------|
| 1    | No cache                      | n/a          | n/a          | 300 cycles  |
| 2    | L1 instruction cache only     | 350 cycles   | n/a          | 300 cycles  |
| 3    | L1 + L2 (instructions only)   | 10 + L2      | 360 cycles   | n/a         |
| 4    | L1 + L2 (data + instructions) | 10 + L2      | 360 cycles   | n/a         |

- **L1 `InstructionCache`**: direct-mapped, one 8-word line, 10-cycle hit
- **L2Cache**: 4-way set-associative with round-robin eviction, 8-word lines, 20-cycle hit, write-through

## Running a program

Programs are assembly text, assembled into binary, packed two instructions per 32-bit word, loaded into memory, and executed.

```java
String[] program = {
    "copy 10 r0",
    "copy 1  r1",
    "add r1 r0",   // r0 = r0 + r1
    "syscall 0",
    "halt"
};

Memory mem = new Memory();
mem.load(Assembler.finalOutput(Assembler.assemble(program)));
Processor cpu = new Processor(mem);   // default: mode 4, full cache
cpu.run();
```

## Building and testing

Requires Java 17 and Maven.

```bash
cd CPU
mvn test
```

37 tests across 11 suites, all passing.

| Test file | What it covers |
|---|---|
| `BitTest` | Bit operations |
| `Word16Test`, `Word32Test` | Word construction and conversion |
| `AdderTest` | Addition and subtraction |
| `MultiplierTest` | Binary multiplication |
| `ShifterTest` | Left and right shifts |
| `ALUTest` | ALU instruction dispatch |
| `MemoryTest` | Load and store |
| `AssemblerTest` | Assembly to binary encoding |
| `ProcessorTest` | End-to-end programs |
| `CacheTest` | The three benchmark programs, across cache modes |

## Project structure

```
.
├── CPU/
│   ├── pom.xml
│   └── src/
│       ├── main/java/      # Bit, words, arithmetic, ALU, memory, caches, assembler, processor
│       └── test/java/      # JUnit 5 suites
├── assignments/            # Assignment specifications
└── CacheReport.pdf         # Cache performance writeup
```

## Notes

Built across six assignments for a computer architecture course (ICSI 404, UAlbany): bits and words, arithmetic, memory and ALU, the assembler, the processor, and the cache hierarchy. The instruction set was specified by the course; the implementation of every layer, the benchmark programs, and the performance analysis are mine.