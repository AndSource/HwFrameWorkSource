package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cmc.CMCStatusInfo.OtherInfo;

public class CMCStatusInfoBuilder {
    private final ASN1Sequence bodyList;
    private final CMCStatus cMCStatus;
    private OtherInfo otherInfo;
    private DERUTF8String statusString;

    public CMCStatusInfoBuilder(CMCStatus cMCStatus, BodyPartID bodyPartID) {
        this.cMCStatus = cMCStatus;
        this.bodyList = new DERSequence((ASN1Encodable) bodyPartID);
    }

    public CMCStatusInfoBuilder(CMCStatus cMCStatus, BodyPartID[] bodyPartIDArr) {
        this.cMCStatus = cMCStatus;
        this.bodyList = new DERSequence((ASN1Encodable[]) bodyPartIDArr);
    }

    public CMCStatusInfo build() {
        return new CMCStatusInfo(this.cMCStatus, this.bodyList, this.statusString, this.otherInfo);
    }

    public CMCStatusInfoBuilder setOtherInfo(CMCFailInfo cMCFailInfo) {
        this.otherInfo = new OtherInfo(cMCFailInfo);
        return this;
    }

    public CMCStatusInfoBuilder setOtherInfo(PendInfo pendInfo) {
        this.otherInfo = new OtherInfo(pendInfo);
        return this;
    }

    public CMCStatusInfoBuilder setStatusString(String str) {
        this.statusString = new DERUTF8String(str);
        return this;
    }
}
