package org.bouncycastle.pqc.jcajce.provider.mceliece;

import java.io.IOException;
import java.security.PublicKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.McElieceCCA2PublicKey;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PublicKeyParameters;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;

public class BCMcElieceCCA2PublicKey implements CipherParameters, PublicKey {
    private static final long serialVersionUID = 1;
    private McElieceCCA2PublicKeyParameters params;

    public BCMcElieceCCA2PublicKey(McElieceCCA2PublicKeyParameters mcElieceCCA2PublicKeyParameters) {
        this.params = mcElieceCCA2PublicKeyParameters;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj != null) {
            if (!(obj instanceof BCMcElieceCCA2PublicKey)) {
                return false;
            }
            BCMcElieceCCA2PublicKey bCMcElieceCCA2PublicKey = (BCMcElieceCCA2PublicKey) obj;
            if (this.params.getN() == bCMcElieceCCA2PublicKey.getN() && this.params.getT() == bCMcElieceCCA2PublicKey.getT() && this.params.getG().equals(bCMcElieceCCA2PublicKey.getG())) {
                z = true;
            }
        }
        return z;
    }

    public String getAlgorithm() {
        return "McEliece-CCA2";
    }

    public byte[] getEncoded() {
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2), new McElieceCCA2PublicKey(this.params.getN(), this.params.getT(), this.params.getG(), Utils.getDigAlgId(this.params.getDigest()))).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public GF2Matrix getG() {
        return this.params.getG();
    }

    public int getK() {
        return this.params.getK();
    }

    AsymmetricKeyParameter getKeyParams() {
        return this.params;
    }

    public int getN() {
        return this.params.getN();
    }

    public int getT() {
        return this.params.getT();
    }

    public int hashCode() {
        return (37 * (this.params.getN() + (this.params.getT() * 37))) + this.params.getG().hashCode();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("McEliecePublicKey:\n");
        stringBuilder.append(" length of the code         : ");
        stringBuilder.append(this.params.getN());
        stringBuilder.append("\n");
        String stringBuilder2 = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(stringBuilder2);
        stringBuilder.append(" error correction capability: ");
        stringBuilder.append(this.params.getT());
        stringBuilder.append("\n");
        stringBuilder2 = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(stringBuilder2);
        stringBuilder.append(" generator matrix           : ");
        stringBuilder.append(this.params.getG().toString());
        return stringBuilder.toString();
    }
}
