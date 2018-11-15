package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat128;

public class SecT113Field {
    private static final long M49 = 562949953421311L;
    private static final long M57 = 144115188075855871L;

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr2[1] ^ jArr[1];
    }

    public static void addExt(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr2[3] ^ jArr[3];
    }

    public static void addOne(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0] ^ 1;
        jArr2[1] = jArr[1];
    }

    public static long[] fromBigInteger(BigInteger bigInteger) {
        long[] fromBigInteger64 = Nat128.fromBigInteger64(bigInteger);
        reduce15(fromBigInteger64, 0);
        return fromBigInteger64;
    }

    protected static void implMultiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long j = jArr[0];
        long j2 = ((jArr[1] << 7) ^ (j >>> 57)) & M57;
        j &= M57;
        long j3 = jArr2[0];
        long j4 = ((jArr2[1] << 7) ^ (j3 >>> 57)) & M57;
        long j5 = M57 & j3;
        long[] jArr4 = new long[6];
        long[] jArr5 = jArr4;
        implMulw(j, j5, jArr5, 0);
        implMulw(j2, j4, jArr5, 2);
        implMulw(j ^ j2, j5 ^ j4, jArr5, 4);
        j = jArr4[1] ^ jArr4[2];
        long j6 = jArr4[0];
        j3 = jArr4[3];
        long j7 = (jArr4[4] ^ j6) ^ j;
        j ^= jArr4[5] ^ j3;
        jArr3[0] = (j7 << 57) ^ j6;
        jArr3[1] = (j7 >>> 7) ^ (j << 50);
        jArr3[2] = (j >>> 14) ^ (j3 << 43);
        jArr3[3] = j3 >>> 21;
    }

    protected static void implMulw(long j, long j2, long[] jArr, int i) {
        long j3 = j;
        long[] jArr2 = new long[8];
        jArr2[1] = j2;
        jArr2[2] = jArr2[1] << 1;
        jArr2[3] = jArr2[2] ^ j2;
        jArr2[4] = jArr2[2] << 1;
        jArr2[5] = jArr2[4] ^ j2;
        jArr2[6] = jArr2[3] << 1;
        jArr2[7] = jArr2[6] ^ j2;
        long j4 = 48;
        long j5 = 0;
        long j6 = jArr2[((int) j3) & 7];
        do {
            int i2 = (int) (j3 >>> j4);
            long j7 = (jArr2[i2 & 7] ^ (jArr2[(i2 >>> 3) & 7] << 3)) ^ (jArr2[(i2 >>> 6) & 7] << 6);
            j6 ^= j7 << j4;
            j5 ^= j7 >>> (-j4);
            j4 -= 9;
        } while (j4 > null);
        j3 = (((j3 & 72198606942111744L) & ((j2 << 7) >> 63)) >>> 8) ^ j5;
        jArr[i] = M57 & j6;
        jArr[i + 1] = (j3 << 7) ^ (j6 >>> 57);
    }

    protected static void implSquare(long[] jArr, long[] jArr2) {
        Interleave.expand64To128(jArr[0], jArr2, 0);
        Interleave.expand64To128(jArr[1], jArr2, 2);
    }

    public static void invert(long[] jArr, long[] jArr2) {
        if (Nat128.isZero64(jArr)) {
            throw new IllegalStateException();
        }
        long[] create64 = Nat128.create64();
        long[] create642 = Nat128.create64();
        square(jArr, create64);
        multiply(create64, jArr, create64);
        square(create64, create64);
        multiply(create64, jArr, create64);
        squareN(create64, 3, create642);
        multiply(create642, create64, create642);
        square(create642, create642);
        multiply(create642, jArr, create642);
        squareN(create642, 7, create64);
        multiply(create64, create642, create64);
        squareN(create64, 14, create642);
        multiply(create642, create64, create642);
        squareN(create642, 28, create64);
        multiply(create64, create642, create64);
        squareN(create64, 56, create642);
        multiply(create642, create64, create642);
        square(create642, jArr2);
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat128.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        reduce(createExt64, jArr3);
    }

    public static void multiplyAddToExt(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat128.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        addExt(jArr3, createExt64, jArr3);
    }

    public static void reduce(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        j3 ^= (j4 >>> 40) ^ (j4 >>> 49);
        j ^= (j3 << 15) ^ (j3 << 24);
        j2 = (j2 ^ ((j4 << 15) ^ (j4 << 24))) ^ ((j3 >>> 40) ^ (j3 >>> 49));
        j3 = j2 >>> 49;
        jArr2[0] = (j ^ j3) ^ (j3 << 9);
        jArr2[1] = M49 & j2;
    }

    public static void reduce15(long[] jArr, int i) {
        int i2 = i + 1;
        long j = jArr[i2];
        long j2 = j >>> 49;
        jArr[i] = (j2 ^ (j2 << 9)) ^ jArr[i];
        jArr[i2] = j & M49;
    }

    public static void sqrt(long[] jArr, long[] jArr2) {
        long unshuffle = Interleave.unshuffle(jArr[0]);
        long unshuffle2 = Interleave.unshuffle(jArr[1]);
        unshuffle = (unshuffle >>> 32) | (unshuffle2 & -4294967296L);
        jArr2[0] = ((unshuffle << 57) ^ ((BodyPartID.bodyIdMax & unshuffle) | (unshuffle2 << 32))) ^ (unshuffle << 5);
        jArr2[1] = (unshuffle >>> 59) ^ (unshuffle >>> 7);
    }

    public static void square(long[] jArr, long[] jArr2) {
        long[] createExt64 = Nat128.createExt64();
        implSquare(jArr, createExt64);
        reduce(createExt64, jArr2);
    }

    public static void squareAddToExt(long[] jArr, long[] jArr2) {
        long[] createExt64 = Nat128.createExt64();
        implSquare(jArr, createExt64);
        addExt(jArr2, createExt64, jArr2);
    }

    public static void squareN(long[] jArr, int i, long[] jArr2) {
        long[] createExt64 = Nat128.createExt64();
        implSquare(jArr, createExt64);
        while (true) {
            reduce(createExt64, jArr2);
            i--;
            if (i > 0) {
                implSquare(jArr2, createExt64);
            } else {
                return;
            }
        }
    }

    public static int trace(long[] jArr) {
        return ((int) jArr[0]) & 1;
    }
}
