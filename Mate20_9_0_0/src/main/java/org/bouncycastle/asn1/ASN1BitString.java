package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public abstract class ASN1BitString extends ASN1Primitive implements ASN1String {
    private static final char[] table = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    protected final byte[] data;
    protected final int padBits;

    public ASN1BitString(byte[] bArr, int i) {
        if (bArr == null) {
            throw new NullPointerException("data cannot be null");
        } else if (bArr.length == 0 && i != 0) {
            throw new IllegalArgumentException("zero length data with non-zero pad bits");
        } else if (i > 7 || i < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        } else {
            this.data = Arrays.clone(bArr);
            this.padBits = i;
        }
    }

    protected static byte[] derForm(byte[] bArr, int i) {
        byte[] clone = Arrays.clone(bArr);
        if (i > 0) {
            int length = bArr.length - 1;
            clone[length] = (byte) ((255 << i) & clone[length]);
        }
        return clone;
    }

    static ASN1BitString fromInputStream(int i, InputStream inputStream) throws IOException {
        if (i >= 1) {
            int read = inputStream.read();
            byte[] bArr = new byte[(i - 1)];
            if (bArr.length != 0) {
                if (Streams.readFully(inputStream, bArr) != bArr.length) {
                    throw new EOFException("EOF encountered in middle of BIT STRING");
                } else if (read > 0 && read < 8 && bArr[bArr.length - 1] != ((byte) (bArr[bArr.length - 1] & (255 << read)))) {
                    return new DLBitString(bArr, read);
                }
            }
            return new DERBitString(bArr, read);
        }
        throw new IllegalArgumentException("truncated BIT STRING detected");
    }

    protected static byte[] getBytes(int i) {
        int i2 = 0;
        if (i == 0) {
            return new byte[0];
        }
        int i3 = 4;
        int i4 = 3;
        while (i4 >= 1 && ((255 << (i4 * 8)) & i) == 0) {
            i3--;
            i4--;
        }
        byte[] bArr = new byte[i3];
        while (i2 < i3) {
            bArr[i2] = (byte) ((i >> (i2 * 8)) & 255);
            i2++;
        }
        return bArr;
    }

    protected static int getPadBits(int i) {
        for (int i2 = 3; i2 >= 0; i2--) {
            if (i2 != 0) {
                int i3 = i >> (i2 * 8);
                if (i3 != 0) {
                    i = i3 & 255;
                    break;
                }
            } else if (i != 0) {
                i &= 255;
                break;
            }
        }
        i = 0;
        if (i == 0) {
            return 0;
        }
        int i4 = 1;
        while (true) {
            i <<= 1;
            if ((i & 255) == 0) {
                return 8 - i4;
            }
            i4++;
        }
    }

    protected boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        boolean z = false;
        if (!(aSN1Primitive instanceof ASN1BitString)) {
            return false;
        }
        ASN1BitString aSN1BitString = (ASN1BitString) aSN1Primitive;
        if (this.padBits == aSN1BitString.padBits && Arrays.areEqual(getBytes(), aSN1BitString.getBytes())) {
            z = true;
        }
        return z;
    }

    abstract void encode(ASN1OutputStream aSN1OutputStream) throws IOException;

    public byte[] getBytes() {
        return derForm(this.data, this.padBits);
    }

    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    public byte[] getOctets() {
        if (this.padBits == 0) {
            return Arrays.clone(this.data);
        }
        throw new IllegalStateException("attempt to get non-octet aligned data from BIT STRING");
    }

    public int getPadBits() {
        return this.padBits;
    }

    public String getString() {
        StringBuffer stringBuffer = new StringBuffer("#");
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            new ASN1OutputStream(byteArrayOutputStream).writeObject(this);
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            for (int i = 0; i != toByteArray.length; i++) {
                stringBuffer.append(table[(toByteArray[i] >>> 4) & 15]);
                stringBuffer.append(table[toByteArray[i] & 15]);
            }
            return stringBuffer.toString();
        } catch (Throwable e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Internal error encoding BitString: ");
            stringBuilder.append(e.getMessage());
            throw new ASN1ParsingException(stringBuilder.toString(), e);
        }
    }

    public int hashCode() {
        return this.padBits ^ Arrays.hashCode(getBytes());
    }

    public int intValue() {
        byte[] bArr = this.data;
        if (this.padBits > 0 && this.data.length <= 4) {
            bArr = derForm(this.data, this.padBits);
        }
        int i = 0;
        int i2 = 0;
        while (i != bArr.length && i != 4) {
            i2 |= (bArr[i] & 255) << (8 * i);
            i++;
        }
        return i2;
    }

    ASN1Primitive toDERObject() {
        return new DERBitString(this.data, this.padBits);
    }

    ASN1Primitive toDLObject() {
        return new DLBitString(this.data, this.padBits);
    }

    public String toString() {
        return getString();
    }
}
