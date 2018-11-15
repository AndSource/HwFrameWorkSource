package org.bouncycastle.asn1.dvcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class TargetEtcChain extends ASN1Object {
    private ASN1Sequence chain;
    private PathProcInput pathProcInput;
    private CertEtcToken target;

    private TargetEtcChain(ASN1Sequence aSN1Sequence) {
        this.target = CertEtcToken.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            ASN1Encodable objectAt = aSN1Sequence.getObjectAt(1);
            if (objectAt instanceof ASN1TaggedObject) {
                extractPathProcInput(objectAt);
                return;
            }
            this.chain = ASN1Sequence.getInstance(objectAt);
            if (aSN1Sequence.size() > 2) {
                extractPathProcInput(aSN1Sequence.getObjectAt(2));
            }
        }
    }

    public TargetEtcChain(CertEtcToken certEtcToken) {
        this(certEtcToken, null, null);
    }

    public TargetEtcChain(CertEtcToken certEtcToken, PathProcInput pathProcInput) {
        this(certEtcToken, null, pathProcInput);
    }

    public TargetEtcChain(CertEtcToken certEtcToken, CertEtcToken[] certEtcTokenArr) {
        this(certEtcToken, certEtcTokenArr, null);
    }

    public TargetEtcChain(CertEtcToken certEtcToken, CertEtcToken[] certEtcTokenArr, PathProcInput pathProcInput) {
        this.target = certEtcToken;
        if (certEtcTokenArr != null) {
            this.chain = new DERSequence((ASN1Encodable[]) certEtcTokenArr);
        }
        this.pathProcInput = pathProcInput;
    }

    public static TargetEtcChain[] arrayFromSequence(ASN1Sequence aSN1Sequence) {
        TargetEtcChain[] targetEtcChainArr = new TargetEtcChain[aSN1Sequence.size()];
        for (int i = 0; i != targetEtcChainArr.length; i++) {
            targetEtcChainArr[i] = getInstance(aSN1Sequence.getObjectAt(i));
        }
        return targetEtcChainArr;
    }

    private void extractPathProcInput(ASN1Encodable aSN1Encodable) {
        ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Encodable);
        if (instance.getTagNo() == 0) {
            this.pathProcInput = PathProcInput.getInstance(instance, false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown tag encountered: ");
        stringBuilder.append(instance.getTagNo());
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static TargetEtcChain getInstance(Object obj) {
        return obj instanceof TargetEtcChain ? (TargetEtcChain) obj : obj != null ? new TargetEtcChain(ASN1Sequence.getInstance(obj)) : null;
    }

    public static TargetEtcChain getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public CertEtcToken[] getChain() {
        return this.chain != null ? CertEtcToken.arrayFromSequence(this.chain) : null;
    }

    public PathProcInput getPathProcInput() {
        return this.pathProcInput;
    }

    public CertEtcToken getTarget() {
        return this.target;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.target);
        if (this.chain != null) {
            aSN1EncodableVector.add(this.chain);
        }
        if (this.pathProcInput != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.pathProcInput));
        }
        return new DERSequence(aSN1EncodableVector);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("TargetEtcChain {\n");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("target: ");
        stringBuilder.append(this.target);
        stringBuilder.append("\n");
        stringBuffer.append(stringBuilder.toString());
        if (this.chain != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("chain: ");
            stringBuilder.append(this.chain);
            stringBuilder.append("\n");
            stringBuffer.append(stringBuilder.toString());
        }
        if (this.pathProcInput != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("pathProcInput: ");
            stringBuilder.append(this.pathProcInput);
            stringBuilder.append("\n");
            stringBuffer.append(stringBuilder.toString());
        }
        stringBuffer.append("}\n");
        return stringBuffer.toString();
    }
}
