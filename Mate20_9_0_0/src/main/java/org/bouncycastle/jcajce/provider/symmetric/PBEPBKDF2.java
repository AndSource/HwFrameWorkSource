package org.bouncycastle.jcajce.provider.symmetric;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.jcajce.PBKDF2Key;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameters;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import org.bouncycastle.util.Integers;

public class PBEPBKDF2 {
    private static final Map prfCodes = new HashMap();

    public static class AlgParams extends BaseAlgorithmParameters {
        PBKDF2Params params;

        protected byte[] engineGetEncoded() {
            try {
                return this.params.getEncoded(ASN1Encoding.DER);
            } catch (IOException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Oooops! ");
                stringBuilder.append(e.toString());
                throw new RuntimeException(stringBuilder.toString());
            }
        }

        protected byte[] engineGetEncoded(String str) {
            return isASN1FormatString(str) ? engineGetEncoded() : null;
        }

        protected void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
            if (algorithmParameterSpec instanceof PBEParameterSpec) {
                PBEParameterSpec pBEParameterSpec = (PBEParameterSpec) algorithmParameterSpec;
                this.params = new PBKDF2Params(pBEParameterSpec.getSalt(), pBEParameterSpec.getIterationCount());
                return;
            }
            throw new InvalidParameterSpecException("PBEParameterSpec required to initialise a PBKDF2 PBE parameters algorithm parameters object");
        }

        protected void engineInit(byte[] bArr) throws IOException {
            this.params = PBKDF2Params.getInstance(ASN1Primitive.fromByteArray(bArr));
        }

        protected void engineInit(byte[] bArr, String str) throws IOException {
            if (isASN1FormatString(str)) {
                engineInit(bArr);
                return;
            }
            throw new IOException("Unknown parameters format in PBKDF2 parameters object");
        }

        protected String engineToString() {
            return "PBKDF2 Parameters";
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException {
            if (cls == PBEParameterSpec.class) {
                return new PBEParameterSpec(this.params.getSalt(), this.params.getIterationCount().intValue());
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PBKDF2 PBE parameters object.");
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PREFIX = PBEPBKDF2.class.getName();

        public void configure(ConfigurableProvider configurableProvider) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$AlgParams");
            configurableProvider.addAlgorithm("AlgorithmParameters.PBKDF2", stringBuilder.toString());
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Alg.Alias.AlgorithmParameters.");
            stringBuilder2.append(PKCSObjectIdentifiers.id_PBKDF2);
            configurableProvider.addAlgorithm(stringBuilder2.toString(), "PBKDF2");
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withUTF8");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2", stringBuilder.toString());
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2WITHHMACSHA1", "PBKDF2");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2WITHHMACSHA1ANDUTF8", "PBKDF2");
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Alg.Alias.SecretKeyFactory.");
            stringBuilder2.append(PKCSObjectIdentifiers.id_PBKDF2);
            configurableProvider.addAlgorithm(stringBuilder2.toString(), "PBKDF2");
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2with8BIT");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHASCII", stringBuilder.toString());
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2WITH8BIT", "PBKDF2WITHASCII");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2WITHHMACSHA1AND8BIT", "PBKDF2WITHASCII");
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA224");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA224", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA256");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA256", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA384");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA384", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA512");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA512", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA3_224");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA3-224", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA3_256");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA3-256", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA3_384");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA3-384", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withSHA3_512");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACSHA3-512", stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(PREFIX);
            stringBuilder.append("$PBKDF2withGOST3411");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBKDF2WITHHMACGOST3411", stringBuilder.toString());
        }
    }

    public static class BasePBKDF2 extends BaseSecretKeyFactory {
        private int defaultDigest;
        private int scheme;

        public BasePBKDF2(String str, int i) {
            this(str, i, 1);
        }

        public BasePBKDF2(String str, int i, int i2) {
            super(str, PKCSObjectIdentifiers.id_PBKDF2);
            this.scheme = i;
            this.defaultDigest = i2;
        }

        private int getDigestCode(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws InvalidKeySpecException {
            Integer num = (Integer) PBEPBKDF2.prfCodes.get(aSN1ObjectIdentifier);
            if (num != null) {
                return num.intValue();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid KeySpec: unknown PRF algorithm ");
            stringBuilder.append(aSN1ObjectIdentifier);
            throw new InvalidKeySpecException(stringBuilder.toString());
        }

        protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof PBEKeySpec) {
                PBEKeySpec pBEKeySpec = (PBEKeySpec) keySpec;
                StringBuilder stringBuilder;
                int digestCode;
                int keyLength;
                if (pBEKeySpec.getSalt() == null) {
                    return new PBKDF2Key(pBEKeySpec.getPassword(), this.scheme == 1 ? PasswordConverter.ASCII : PasswordConverter.UTF8);
                } else if (pBEKeySpec.getIterationCount() <= 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("positive iteration count required: ");
                    stringBuilder.append(pBEKeySpec.getIterationCount());
                    throw new InvalidKeySpecException(stringBuilder.toString());
                } else if (pBEKeySpec.getKeyLength() <= 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("positive key length required: ");
                    stringBuilder.append(pBEKeySpec.getKeyLength());
                    throw new InvalidKeySpecException(stringBuilder.toString());
                } else if (pBEKeySpec.getPassword().length == 0) {
                    throw new IllegalArgumentException("password empty");
                } else if (pBEKeySpec instanceof PBKDF2KeySpec) {
                    digestCode = getDigestCode(((PBKDF2KeySpec) pBEKeySpec).getPrf().getAlgorithm());
                    keyLength = pBEKeySpec.getKeyLength();
                    return new BCPBEKey(this.algName, this.algOid, this.scheme, digestCode, keyLength, -1, pBEKeySpec, Util.makePBEMacParameters(pBEKeySpec, this.scheme, digestCode, keyLength));
                } else {
                    digestCode = this.defaultDigest;
                    keyLength = pBEKeySpec.getKeyLength();
                    return new BCPBEKey(this.algName, this.algOid, this.scheme, digestCode, keyLength, -1, pBEKeySpec, Util.makePBEMacParameters(pBEKeySpec, this.scheme, digestCode, keyLength));
                }
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }

    public static class PBKDF2with8BIT extends BasePBKDF2 {
        public PBKDF2with8BIT() {
            super("PBKDF2", 1);
        }
    }

    public static class PBKDF2withGOST3411 extends BasePBKDF2 {
        public PBKDF2withGOST3411() {
            super("PBKDF2", 5, 6);
        }
    }

    public static class PBKDF2withSHA224 extends BasePBKDF2 {
        public PBKDF2withSHA224() {
            super("PBKDF2", 5, 7);
        }
    }

    public static class PBKDF2withSHA256 extends BasePBKDF2 {
        public PBKDF2withSHA256() {
            super("PBKDF2", 5, 4);
        }
    }

    public static class PBKDF2withSHA384 extends BasePBKDF2 {
        public PBKDF2withSHA384() {
            super("PBKDF2", 5, 8);
        }
    }

    public static class PBKDF2withSHA3_224 extends BasePBKDF2 {
        public PBKDF2withSHA3_224() {
            super("PBKDF2", 5, 10);
        }
    }

    public static class PBKDF2withSHA3_256 extends BasePBKDF2 {
        public PBKDF2withSHA3_256() {
            super("PBKDF2", 5, 11);
        }
    }

    public static class PBKDF2withSHA3_384 extends BasePBKDF2 {
        public PBKDF2withSHA3_384() {
            super("PBKDF2", 5, 12);
        }
    }

    public static class PBKDF2withSHA3_512 extends BasePBKDF2 {
        public PBKDF2withSHA3_512() {
            super("PBKDF2", 5, 13);
        }
    }

    public static class PBKDF2withSHA512 extends BasePBKDF2 {
        public PBKDF2withSHA512() {
            super("PBKDF2", 5, 9);
        }
    }

    public static class PBKDF2withUTF8 extends BasePBKDF2 {
        public PBKDF2withUTF8() {
            super("PBKDF2", 5);
        }
    }

    static {
        prfCodes.put(CryptoProObjectIdentifiers.gostR3411Hmac, Integers.valueOf(6));
        prfCodes.put(PKCSObjectIdentifiers.id_hmacWithSHA1, Integers.valueOf(1));
        prfCodes.put(PKCSObjectIdentifiers.id_hmacWithSHA256, Integers.valueOf(4));
        prfCodes.put(PKCSObjectIdentifiers.id_hmacWithSHA224, Integers.valueOf(7));
        prfCodes.put(PKCSObjectIdentifiers.id_hmacWithSHA384, Integers.valueOf(8));
        prfCodes.put(PKCSObjectIdentifiers.id_hmacWithSHA512, Integers.valueOf(9));
        prfCodes.put(NISTObjectIdentifiers.id_hmacWithSHA3_256, Integers.valueOf(11));
        prfCodes.put(NISTObjectIdentifiers.id_hmacWithSHA3_224, Integers.valueOf(10));
        prfCodes.put(NISTObjectIdentifiers.id_hmacWithSHA3_384, Integers.valueOf(12));
        prfCodes.put(NISTObjectIdentifiers.id_hmacWithSHA3_512, Integers.valueOf(13));
    }

    private PBEPBKDF2() {
    }
}
