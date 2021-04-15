package ehn.techiop.hcert.kotlin.chain.impl

import COSE.AlgorithmID
import COSE.HeaderKeys
import COSE.OneKey
import com.upokecenter.cbor.CBORObject
import ehn.techiop.hcert.kotlin.chain.CryptoService
import ehn.techiop.hcert.kotlin.chain.asBase64Url
import org.bouncycastle.asn1.x500.X500Name
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.X509Certificate

class RandomEcKeyCryptoService : CryptoService {

    private val keyPair = KeyPairGenerator.getInstance("EC")
        .apply { initialize(256) }.genKeyPair()
    private val keyPairCert: X509Certificate = PkiUtils().selfSignCertificate(X500Name("CN=EC-Me"), keyPair)

    private val keyId: String = MessageDigest.getInstance("SHA-256")
        .digest(keyPairCert.encoded)
        .copyOf(8).asBase64Url()

    override fun getCborHeaders() = listOf(
        Pair(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR()),
        Pair(HeaderKeys.KID, CBORObject.FromObject(keyId))
    )

    override fun getCborSigningKey() = OneKey(keyPair.public, keyPair.private)

    override fun getCborVerificationKey(kid: String): OneKey {
        if (kid != keyId) throw IllegalArgumentException("kid not known: $kid")
        return OneKey(keyPair.public, keyPair.private)
    }

    override fun getCertificate(kid: String): Certificate {
        if (kid != keyId) throw IllegalArgumentException("kid not known: $kid")
        return keyPairCert
    }

}

