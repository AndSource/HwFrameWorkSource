package org.bouncycastle.jce.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.bouncycastle.jce.X509LDAPCertStoreParameters;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.x509.X509CertPairStoreSelector;
import org.bouncycastle.x509.X509StoreParameters;
import org.bouncycastle.x509.X509StoreSpi;
import org.bouncycastle.x509.util.LDAPStoreHelper;

public class X509StoreLDAPCertPairs extends X509StoreSpi {
    private LDAPStoreHelper helper;

    public Collection engineGetMatches(Selector selector) throws StoreException {
        if (!(selector instanceof X509CertPairStoreSelector)) {
            return Collections.EMPTY_SET;
        }
        X509CertPairStoreSelector x509CertPairStoreSelector = (X509CertPairStoreSelector) selector;
        Collection hashSet = new HashSet();
        hashSet.addAll(this.helper.getCrossCertificatePairs(x509CertPairStoreSelector));
        return hashSet;
    }

    public void engineInit(X509StoreParameters x509StoreParameters) {
        if (x509StoreParameters instanceof X509LDAPCertStoreParameters) {
            this.helper = new LDAPStoreHelper((X509LDAPCertStoreParameters) x509StoreParameters);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Initialization parameters must be an instance of ");
        stringBuilder.append(X509LDAPCertStoreParameters.class.getName());
        stringBuilder.append(".");
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
