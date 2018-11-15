package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

public class CMCStatusInfo extends ASN1Object {
    private final ASN1Sequence bodyList;
    private final CMCStatus cMCStatus;
    private final OtherInfo otherInfo;
    private final DERUTF8String statusString;

    public static class OtherInfo extends ASN1Object implements ASN1Choice {
        private final CMCFailInfo failInfo;
        private final PendInfo pendInfo;

        OtherInfo(CMCFailInfo cMCFailInfo) {
            this(cMCFailInfo, null);
        }

        private OtherInfo(CMCFailInfo cMCFailInfo, PendInfo pendInfo) {
            this.failInfo = cMCFailInfo;
            this.pendInfo = pendInfo;
        }

        OtherInfo(PendInfo pendInfo) {
            this(null, pendInfo);
        }

        private static OtherInfo getInstance(Object obj) {
            if (obj instanceof OtherInfo) {
                return (OtherInfo) obj;
            }
            if (obj instanceof ASN1Encodable) {
                ASN1Primitive toASN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
                if (toASN1Primitive instanceof ASN1Integer) {
                    return new OtherInfo(CMCFailInfo.getInstance(toASN1Primitive));
                }
                if (toASN1Primitive instanceof ASN1Sequence) {
                    return new OtherInfo(PendInfo.getInstance(toASN1Primitive));
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unknown object in getInstance(): ");
            stringBuilder.append(obj.getClass().getName());
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        public boolean isFailInfo() {
            return this.failInfo != null;
        }

        public ASN1Primitive toASN1Primitive() {
            return this.pendInfo != null ? this.pendInfo.toASN1Primitive() : this.failInfo.toASN1Primitive();
        }
    }

    private CMCStatusInfo(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 2 || aSN1Sequence.size() > 4) {
            throw new IllegalArgumentException("incorrect sequence size");
        }
        Object objectAt;
        this.cMCStatus = CMCStatus.getInstance(aSN1Sequence.getObjectAt(0));
        this.bodyList = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
        if (aSN1Sequence.size() > 3) {
            this.statusString = DERUTF8String.getInstance(aSN1Sequence.getObjectAt(2));
            objectAt = aSN1Sequence.getObjectAt(3);
        } else {
            if (aSN1Sequence.size() <= 2) {
                this.statusString = null;
            } else if (aSN1Sequence.getObjectAt(2) instanceof DERUTF8String) {
                this.statusString = DERUTF8String.getInstance(aSN1Sequence.getObjectAt(2));
            } else {
                this.statusString = null;
                objectAt = aSN1Sequence.getObjectAt(2);
            }
            this.otherInfo = null;
            return;
        }
        this.otherInfo = OtherInfo.getInstance(objectAt);
    }

    CMCStatusInfo(CMCStatus cMCStatus, ASN1Sequence aSN1Sequence, DERUTF8String dERUTF8String, OtherInfo otherInfo) {
        this.cMCStatus = cMCStatus;
        this.bodyList = aSN1Sequence;
        this.statusString = dERUTF8String;
        this.otherInfo = otherInfo;
    }

    public static CMCStatusInfo getInstance(Object obj) {
        return obj instanceof CMCStatusInfo ? (CMCStatusInfo) obj : obj != null ? new CMCStatusInfo(ASN1Sequence.getInstance(obj)) : null;
    }

    public BodyPartID[] getBodyList() {
        return Utils.toBodyPartIDArray(this.bodyList);
    }

    public CMCStatus getCMCStatus() {
        return this.cMCStatus;
    }

    public OtherInfo getOtherInfo() {
        return this.otherInfo;
    }

    public DERUTF8String getStatusString() {
        return this.statusString;
    }

    public boolean hasOtherInfo() {
        return this.otherInfo != null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.cMCStatus);
        aSN1EncodableVector.add(this.bodyList);
        if (this.statusString != null) {
            aSN1EncodableVector.add(this.statusString);
        }
        if (this.otherInfo != null) {
            aSN1EncodableVector.add(this.otherInfo);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
