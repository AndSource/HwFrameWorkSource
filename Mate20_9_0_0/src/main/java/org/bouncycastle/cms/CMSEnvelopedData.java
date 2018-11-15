package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.cms.EnvelopedData;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Encodable;

public class CMSEnvelopedData implements Encodable {
    ContentInfo contentInfo;
    private AlgorithmIdentifier encAlg;
    private OriginatorInformation originatorInfo;
    RecipientInformationStore recipientInfoStore;
    private ASN1Set unprotectedAttributes;

    public CMSEnvelopedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSEnvelopedData(ContentInfo contentInfo) throws CMSException {
        this.contentInfo = contentInfo;
        try {
            EnvelopedData instance = EnvelopedData.getInstance(contentInfo.getContent());
            if (instance.getOriginatorInfo() != null) {
                this.originatorInfo = new OriginatorInformation(instance.getOriginatorInfo());
            }
            ASN1Set recipientInfos = instance.getRecipientInfos();
            EncryptedContentInfo encryptedContentInfo = instance.getEncryptedContentInfo();
            this.encAlg = encryptedContentInfo.getContentEncryptionAlgorithm();
            this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.encAlg, new CMSEnvelopedSecureReadable(this.encAlg, new CMSProcessableByteArray(encryptedContentInfo.getEncryptedContent().getOctets())));
            this.unprotectedAttributes = instance.getUnprotectedAttrs();
        } catch (Exception e) {
            throw new CMSException("Malformed content.", e);
        } catch (Exception e2) {
            throw new CMSException("Malformed content.", e2);
        }
    }

    public CMSEnvelopedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        return aSN1Encodable != null ? aSN1Encodable.toASN1Primitive().getEncoded() : null;
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return this.encAlg;
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }

    public String getEncryptionAlgOID() {
        return this.encAlg.getAlgorithm().getId();
    }

    public byte[] getEncryptionAlgParams() {
        try {
            return encodeObj(this.encAlg.getParameters());
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("exception getting encryption parameters ");
            stringBuilder.append(e);
            throw new RuntimeException(stringBuilder.toString());
        }
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getUnprotectedAttributes() {
        return this.unprotectedAttributes == null ? null : new AttributeTable(this.unprotectedAttributes);
    }

    public ContentInfo toASN1Structure() {
        return this.contentInfo;
    }
}
