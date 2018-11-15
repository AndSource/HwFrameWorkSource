package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class CBCBlockCipher implements BlockCipher {
    private byte[] IV;
    private int blockSize;
    private byte[] cbcNextV;
    private byte[] cbcV;
    private BlockCipher cipher = null;
    private boolean encrypting;

    public CBCBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
        this.IV = new byte[this.blockSize];
        this.cbcV = new byte[this.blockSize];
        this.cbcNextV = new byte[this.blockSize];
    }

    private int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i <= bArr.length) {
            int i3 = 0;
            System.arraycopy(bArr, i, this.cbcNextV, 0, this.blockSize);
            int processBlock = this.cipher.processBlock(bArr, i, bArr2, i2);
            while (i3 < this.blockSize) {
                i = i2 + i3;
                bArr2[i] = (byte) (bArr2[i] ^ this.cbcV[i3]);
                i3++;
            }
            byte[] bArr3 = this.cbcV;
            this.cbcV = this.cbcNextV;
            this.cbcNextV = bArr3;
            return processBlock;
        }
        throw new DataLengthException("input buffer too short");
    }

    private int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i <= bArr.length) {
            for (int i3 = 0; i3 < this.blockSize; i3++) {
                byte[] bArr3 = this.cbcV;
                bArr3[i3] = (byte) (bArr3[i3] ^ bArr[i + i3]);
            }
            int processBlock = this.cipher.processBlock(this.cbcV, 0, bArr2, i2);
            System.arraycopy(bArr2, i2, this.cbcV, 0, this.cbcV.length);
            return processBlock;
        }
        throw new DataLengthException("input buffer too short");
    }

    public String getAlgorithmName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.cipher.getAlgorithmName());
        stringBuilder.append("/CBC");
        return stringBuilder.toString();
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        boolean z2 = this.encrypting;
        this.encrypting = z;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            Object iv = parametersWithIV.getIV();
            if (iv.length == this.blockSize) {
                System.arraycopy(iv, 0, this.IV, 0, iv.length);
                reset();
                if (parametersWithIV.getParameters() != null) {
                    blockCipher = this.cipher;
                    cipherParameters = parametersWithIV.getParameters();
                } else if (z2 != z) {
                    throw new IllegalArgumentException("cannot change encrypting state without providing key.");
                } else {
                    return;
                }
            }
            throw new IllegalArgumentException("initialisation vector must be the same length as block size");
        }
        reset();
        if (cipherParameters != null) {
            blockCipher = this.cipher;
        } else if (z2 != z) {
            throw new IllegalArgumentException("cannot change encrypting state without providing key.");
        } else {
            return;
        }
        blockCipher.init(z, cipherParameters);
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        return this.encrypting ? encryptBlock(bArr, i, bArr2, i2) : decryptBlock(bArr, i, bArr2, i2);
    }

    public void reset() {
        System.arraycopy(this.IV, 0, this.cbcV, 0, this.IV.length);
        Arrays.fill(this.cbcNextV, (byte) 0);
        this.cipher.reset();
    }
}
