package ehn.techiop.hcert.kotlin.chain.impl

import COSE.AlgorithmID
import COSE.HeaderKeys
import COSE.OneKey
import com.upokecenter.cbor.CBORObject
import ehn.techiop.hcert.kotlin.chain.CryptoService
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate

class RandomRsa3072KeyCryptoService : CryptoService {

    init {
        Security.addProvider(BouncyCastleProvider()) // for SHA256withRSA/PSS
    }

    private val keyPair = KeyPairGenerator.getInstance("RSA")
        .apply { initialize(3072) }.genKeyPair()
    private val keyPairCert: X509Certificate = PkiUtils().selfSignCertificate(X500Name("CN=RSA-Me"), keyPair)

    private val keyId = MessageDigest.getInstance("SHA-256")
        .digest(keyPairCert.encoded)
        .copyOf(8)

    override fun getCborHeaders() = listOf(
        Pair(HeaderKeys.Algorithm, AlgorithmID.RSA_PSS_256.AsCBOR()),
        Pair(HeaderKeys.KID, CBORObject.FromObject(keyId))
    )

    override fun getCborSigningKey() = OneKey(keyPair.public, keyPair.private)

    override fun getCborVerificationKey(kid: ByteArray): OneKey {
        if (!(keyId contentEquals kid)) throw IllegalArgumentException("kid not known: $kid")
        return OneKey(keyPair.public, keyPair.private)
    }

    override fun getCertificate(kid: ByteArray): Certificate {
        if (!(keyId contentEquals kid)) throw IllegalArgumentException("kid not known: $kid")
        return keyPairCert
    }

}

