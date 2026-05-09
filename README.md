# ICSI 404 – Computer Architecture: CPU Simulator

A ground-up CPU simulator built in Java for ICSI 404. The project implements a custom 16-bit instruction set architecture (ISA), a full fetch-decode-execute-store pipeline, an assembler, and a two-level cache hierarchy.

## Project Structure

```
ICSI404/
├── CPU/                        # Maven project
│   ├── src/main/java/
│   │   ├── Bit.java            # Single-bit type
│   │   ├── Word16.java         # 16-bit word (instructions)
│   │   ├── Word32.java         # 32-bit word (data/addresses)
│   │   ├── Adder.java          # Ripple-carry adder/subtractor
│   │   ├── Multiplier.java     # Binary multiplier
│   │   ├── Shifter.java        # Left/right bit shifter
│   │   ├── ALU.java            # Arithmetic Logic Unit
│   │   ├── Memory.java         # 1000-word simulated DRAM
│   │   ├── InstructionCache.java  # L1 instruction cache (8 words)
│   │   ├── L2Cache.java        # L2 unified cache (4 lines × 8 words)
│   │   ├── Assembler.java      # Text assembler → binary
│   │   ├── Processor.java      # Pipelined CPU with configurable cache mode
│   │   └── TestConverter.java  # int ↔ Word32 utility
│   └── src/test/java/          # JUnit 5 test suite
└── assignments/                # Assignment spec documents
```

## Instruction Set Architecture

Instructions are 16 bits wide. Two instructions are packed into each 32-bit memory word.

| Field  | Bits | Description                        |
|--------|------|------------------------------------|
| opcode | 5    | Operation                          |
| mode   | 1    | 0 = register operand, 1 = immediate |
| opA    | 5    | Source register or immediate value |
| opB    | 5    | Destination register               |

### Supported Instructions

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
| 8      | syscall    | 0 = print registers, 1 = print memory |
| 9      | call       | Push PC+1, jump to PC + immediate      |
| 10     | return     | Pop and jump to return address         |
| 11     | compare    | Set less/equal flags (opB vs opA)      |
| 12     | ble        | Branch if less-or-equal                |
| 13     | blt        | Branch if less-than                    |
| 14     | bge        | Branch if greater-or-equal             |
| 15     | bgt        | Branch if greater-than                 |
| 16     | beq        | Branch if equal                        |
| 17     | bne        | Branch if not-equal                    |
| 18     | load       | opB = Memory[opB + opA]               |
| 19     | store      | Memory[opB] = opA                      |
| 20     | copy       | opB = opA                              |

## Cache Hierarchy

The processor supports four cache modes (set via the `cacheMode` field):

| Mode | Configuration           | IC miss cost | L2 miss cost | Memory cost |
|------|-------------------------|--------------|--------------|-------------|
| 1    | No cache                | —            | —            | 300 cycles  |
| 2    | L1 instruction cache only | 350 cycles  | —            | 300 cycles  |
| 3    | L1 + L2 (instructions only) | 10 + L2  | 360 cycles   | —           |
| 4    | L1 + L2 (data + instructions) | 10 + L2 | 360 cycles  | —           |

- **L1 InstructionCache** – direct-mapped, 8-word cache line, hit cost = 10 cycles
- **L2Cache** – 4-way set-associative with round-robin eviction, 8-word lines, hit cost = 20 cycles; write-through policy

## Building and Running Tests

Requires Java 17 and Maven.

```bash
cd CPU
mvn test
```

Individual test classes:

| Test file         | What it covers                        |
|-------------------|---------------------------------------|
| BitTest           | Bit operations                        |
| Word16Test        | 16-bit word manipulation              |
| Word32Test        | 32-bit word manipulation              |
| AdderTest         | Addition and subtraction              |
| MultiplierTest    | Binary multiplication                 |
| ShifterTest       | Left/right shifts                     |
| ALUTest           | Full ALU instruction dispatch         |
| AssemblerTest     | Assembly → binary encoding            |
| ProcessorTest     | End-to-end programs (fibonacci, power, etc.) |
| CacheTest         | Cache hit/miss costs across all modes |

## Example Programs

Programs are written as assembly text, assembled with `Assembler.assemble()`, packed into 32-bit words with `Assembler.finalOutput()`, loaded into `Memory`, and executed by `Processor.run()`.

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
Processor cpu = new Processor(mem);   // default: mode 4 (full cache)
cpu.run();
```

## Assignments

| # | Topic                         |
|---|-------------------------------|
| 1 | Bits and Words                |
| 2 | Math (Adder, Multiplier)      |
| 3 | Memory and ALU                |
| 4 | Assembler                     |
| 5 | Processor                     |
| 6 | Cache                         |
