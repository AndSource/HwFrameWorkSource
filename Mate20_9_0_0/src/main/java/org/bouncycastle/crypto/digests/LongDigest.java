package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public abstract class LongDigest implements ExtendedDigest, Memoable, EncodableDigest {
    private static final int BYTE_LENGTH = 128;
    static final long[] K = new long[]{4794697086780616226L, 8158064640168781261L, -5349999486874862801L, -1606136188198331460L, 4131703408338449720L, 6480981068601479193L, -7908458776815382629L, -6116909921290321640L, -2880145864133508542L, 1334009975649890238L, 2608012711638119052L, 6128411473006802146L, 8268148722764581231L, -9160688886553864527L, -7215885187991268811L, -4495734319001033068L, -1973867731355612462L, -1171420211273849373L, 1135362057144423861L, 2597628984639134821L, 3308224258029322869L, 5365058923640841347L, 6679025012923562964L, 8573033837759648693L, -7476448914759557205L, -6327057829258317296L, -5763719355590565569L, -4658551843659510044L, -4116276920077217854L, -3051310485924567259L, 489312712824947311L, 1452737877330783856L, 2861767655752347644L, 3322285676063803686L, 5560940570517711597L, 5996557281743188959L, 7280758554555802590L, 8532644243296465576L, -9096487096722542874L, -7894198246740708037L, -6719396339535248540L, -6333637450476146687L, -4446306890439682159L, -4076793802049405392L, -3345356375505022440L, -2983346525034927856L, -860691631967231958L, 1182934255886127544L, 1847814050463011016L, 2177327727835720531L, 2830643537854262169L, 3796741975233480872L, 4115178125766777443L, 5681478168544905931L, 6601373596472566643L, 7507060721942968483L, 8399075790359081724L, 8693463985226723168L, -8878714635349349518L, -8302665154208450068L, -8016688836872298968L, -6606660893046293015L, -4685533653050689259L, -4147400797238176981L, -3880063495543823972L, -3348786107499101689L, -1523767162380948706L, -757361751448694408L, 500013540394364858L, 748580250866718886L, 1242879168328830382L, 1977374033974150939L, 2944078676154940804L, 3659926193048069267L, 4368137639120453308L, 4836135668995329356L, 5532061633213252278L, 6448918945643986474L, 6902733635092675308L, 7801388544844847127L};
    protected long H1;
    protected long H2;
    protected long H3;
    protected long H4;
    protected long H5;
    protected long H6;
    protected long H7;
    protected long H8;
    private long[] W;
    private long byteCount1;
    private long byteCount2;
    private int wOff;
    private byte[] xBuf;
    private int xBufOff;

    protected LongDigest() {
        this.xBuf = new byte[8];
        this.W = new long[80];
        this.xBufOff = 0;
        reset();
    }

    protected LongDigest(LongDigest longDigest) {
        this.xBuf = new byte[8];
        this.W = new long[80];
        copyIn(longDigest);
    }

    private long Ch(long j, long j2, long j3) {
        return ((~j) & j3) ^ (j2 & j);
    }

    private long Maj(long j, long j2, long j3) {
        return ((j & j3) ^ (j & j2)) ^ (j2 & j3);
    }

    private long Sigma0(long j) {
        return (j >>> 7) ^ (((j << 63) | (j >>> 1)) ^ ((j << 56) | (j >>> 8)));
    }

    private long Sigma1(long j) {
        return (j >>> 6) ^ (((j << 45) | (j >>> 19)) ^ ((j << 3) | (j >>> 61)));
    }

    private long Sum0(long j) {
        return ((j >>> 39) | (j << 25)) ^ (((j << 36) | (j >>> 28)) ^ ((j << 30) | (j >>> 34)));
    }

    private long Sum1(long j) {
        return ((j >>> 41) | (j << 23)) ^ (((j << 50) | (j >>> 14)) ^ ((j << 46) | (j >>> 18)));
    }

    private void adjustByteCounts() {
        if (this.byteCount1 > 2305843009213693951L) {
            this.byteCount2 += this.byteCount1 >>> 61;
            this.byteCount1 &= 2305843009213693951L;
        }
    }

    protected void copyIn(LongDigest longDigest) {
        System.arraycopy(longDigest.xBuf, 0, this.xBuf, 0, longDigest.xBuf.length);
        this.xBufOff = longDigest.xBufOff;
        this.byteCount1 = longDigest.byteCount1;
        this.byteCount2 = longDigest.byteCount2;
        this.H1 = longDigest.H1;
        this.H2 = longDigest.H2;
        this.H3 = longDigest.H3;
        this.H4 = longDigest.H4;
        this.H5 = longDigest.H5;
        this.H6 = longDigest.H6;
        this.H7 = longDigest.H7;
        this.H8 = longDigest.H8;
        System.arraycopy(longDigest.W, 0, this.W, 0, longDigest.W.length);
        this.wOff = longDigest.wOff;
    }

    public void finish() {
        adjustByteCounts();
        long j = this.byteCount1 << 3;
        long j2 = this.byteCount2;
        byte b = Byte.MIN_VALUE;
        while (true) {
            update(b);
            if (this.xBufOff != 0) {
                b = (byte) 0;
            } else {
                processLength(j, j2);
                processBlock();
                return;
            }
        }
    }

    public int getByteLength() {
        return 128;
    }

    protected int getEncodedStateSize() {
        return 96 + (this.wOff * 8);
    }

    protected void populateState(byte[] bArr) {
        int i = 0;
        System.arraycopy(this.xBuf, 0, bArr, 0, this.xBufOff);
        Pack.intToBigEndian(this.xBufOff, bArr, 8);
        Pack.longToBigEndian(this.byteCount1, bArr, 12);
        Pack.longToBigEndian(this.byteCount2, bArr, 20);
        Pack.longToBigEndian(this.H1, bArr, 28);
        Pack.longToBigEndian(this.H2, bArr, 36);
        Pack.longToBigEndian(this.H3, bArr, 44);
        Pack.longToBigEndian(this.H4, bArr, 52);
        Pack.longToBigEndian(this.H5, bArr, 60);
        Pack.longToBigEndian(this.H6, bArr, 68);
        Pack.longToBigEndian(this.H7, bArr, 76);
        Pack.longToBigEndian(this.H8, bArr, 84);
        Pack.intToBigEndian(this.wOff, bArr, 92);
        while (i < this.wOff) {
            Pack.longToBigEndian(this.W[i], bArr, 96 + (i * 8));
            i++;
        }
    }

    protected void processBlock() {
        int i;
        adjustByteCounts();
        for (i = 16; i <= 79; i++) {
            this.W[i] = ((Sigma1(this.W[i - 2]) + this.W[i - 7]) + Sigma0(this.W[i - 15])) + this.W[i - 16];
        }
        long j = this.H1;
        long j2 = this.H2;
        long j3 = this.H3;
        long j4 = this.H4;
        long j5 = this.H5;
        long j6 = this.H6;
        long j7 = j4;
        long j8 = j2;
        long j9 = j3;
        long j10 = j7;
        long j11 = j;
        long j12 = this.H7;
        j7 = j6;
        int i2 = 0;
        int i3 = 0;
        j6 = j5;
        j5 = this.H8;
        while (i2 < 10) {
            long j13 = j11;
            int i4 = i3 + 1;
            j5 += ((Sum1(j6) + Ch(j6, j7, j12)) + K[i3]) + this.W[i3];
            long j14 = j13;
            long j15 = j14;
            j5 += Sum0(j14) + Maj(j14, j8, j9);
            j11 = j10 + j5;
            j13 = j11;
            i3 = i4 + 1;
            j12 += ((Sum1(j11) + Ch(j11, j6, j7)) + K[i4]) + this.W[i4];
            long j16 = j5;
            long j17 = j9 + j12;
            j11 = j12 + (Sum0(j5) + Maj(j5, j15, j8));
            j12 = Sum1(j17);
            long j18 = j17;
            j17 = j11;
            int i5 = i3 + 1;
            j7 += ((j12 + Ch(j17, j13, j6)) + K[i3]) + this.W[i3];
            int i6 = i2;
            long j19 = j8 + j7;
            j11 = j7 + (Sum0(j17) + Maj(j17, j16, j15));
            j7 = Sum1(j19);
            long j20 = j19;
            j19 = j11;
            int i7 = i5 + 1;
            j6 += ((j7 + Ch(j19, j18, j13)) + K[i5]) + this.W[i5];
            j14 = j17;
            long j21 = j17;
            j17 = j15 + j6;
            long Sum0 = j6 + (Sum0(j19) + Maj(j19, j14, j16));
            int i8 = i7 + 1;
            j7 = j13 + (((Sum1(j17) + Ch(j17, j20, j18)) + K[i7]) + this.W[i7]);
            j14 = j19;
            long j22 = j19;
            j19 = j16 + j7;
            j11 = j7 + (Sum0(Sum0) + Maj(Sum0, j14, j21));
            j14 = j17;
            long j23 = j17;
            j17 = j11;
            i7 = i8 + 1;
            j7 = j18 + (((Sum1(j19) + Ch(j19, j14, j20)) + K[i8]) + this.W[i8]);
            long j24 = j21 + j7;
            j14 = j19;
            long j25 = j19;
            j19 = j7 + (Sum0(j17) + Maj(j17, Sum0, j22));
            int i9 = i7 + 1;
            j7 = j20 + (((Sum1(j24) + Ch(j24, j14, j23)) + K[i7]) + this.W[i7]);
            j14 = j17;
            long j26 = j17;
            j17 = j22 + j7;
            j11 = j7 + (Sum0(j19) + Maj(j19, j14, Sum0));
            j7 = Sum1(j17);
            long j27 = j17;
            j17 = j11;
            j7 = j23 + (((j7 + Ch(j17, j24, j25)) + K[i9]) + this.W[i9]);
            Sum0 += j7;
            j11 = j7 + (Sum0(j17) + Maj(j17, j19, j26));
            j9 = j19;
            j8 = j17;
            i3 = i9 + 1;
            j10 = j26;
            j7 = j27;
            i2 = i6 + 1;
            j12 = j24;
            j6 = Sum0;
            j5 = j25;
        }
        this.H1 += j11;
        this.H2 += j8;
        this.H3 += j9;
        this.H4 += j10;
        this.H5 += j6;
        this.H6 += j7;
        this.H7 += j12;
        this.H8 += j5;
        i = 0;
        this.wOff = 0;
        while (i < 16) {
            this.W[i] = 0;
            i++;
        }
    }

    protected void processLength(long j, long j2) {
        if (this.wOff > 14) {
            processBlock();
        }
        this.W[14] = j2;
        this.W[15] = j;
    }

    protected void processWord(byte[] bArr, int i) {
        this.W[this.wOff] = Pack.bigEndianToLong(bArr, i);
        int i2 = this.wOff + 1;
        this.wOff = i2;
        if (i2 == 16) {
            processBlock();
        }
    }

    public void reset() {
        this.byteCount1 = 0;
        this.byteCount2 = 0;
        int i = 0;
        this.xBufOff = 0;
        for (int i2 = 0; i2 < this.xBuf.length; i2++) {
            this.xBuf[i2] = (byte) 0;
        }
        this.wOff = 0;
        while (i != this.W.length) {
            this.W[i] = 0;
            i++;
        }
    }

    protected void restoreState(byte[] bArr) {
        this.xBufOff = Pack.bigEndianToInt(bArr, 8);
        int i = 0;
        System.arraycopy(bArr, 0, this.xBuf, 0, this.xBufOff);
        this.byteCount1 = Pack.bigEndianToLong(bArr, 12);
        this.byteCount2 = Pack.bigEndianToLong(bArr, 20);
        this.H1 = Pack.bigEndianToLong(bArr, 28);
        this.H2 = Pack.bigEndianToLong(bArr, 36);
        this.H3 = Pack.bigEndianToLong(bArr, 44);
        this.H4 = Pack.bigEndianToLong(bArr, 52);
        this.H5 = Pack.bigEndianToLong(bArr, 60);
        this.H6 = Pack.bigEndianToLong(bArr, 68);
        this.H7 = Pack.bigEndianToLong(bArr, 76);
        this.H8 = Pack.bigEndianToLong(bArr, 84);
        this.wOff = Pack.bigEndianToInt(bArr, 92);
        while (i < this.wOff) {
            this.W[i] = Pack.bigEndianToLong(bArr, 96 + (i * 8));
            i++;
        }
    }

    public void update(byte b) {
        byte[] bArr = this.xBuf;
        int i = this.xBufOff;
        this.xBufOff = i + 1;
        bArr[i] = b;
        if (this.xBufOff == this.xBuf.length) {
            processWord(this.xBuf, 0);
            this.xBufOff = 0;
        }
        this.byteCount1++;
    }

    public void update(byte[] bArr, int i, int i2) {
        while (this.xBufOff != 0 && i2 > 0) {
            update(bArr[i]);
            i++;
            i2--;
        }
        while (i2 > this.xBuf.length) {
            processWord(bArr, i);
            i += this.xBuf.length;
            i2 -= this.xBuf.length;
            this.byteCount1 += (long) this.xBuf.length;
        }
        while (i2 > 0) {
            update(bArr[i]);
            i++;
            i2--;
        }
    }
}
