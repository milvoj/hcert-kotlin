package ehn.techiop.hcert.kotlin.chain.common

import Asn1js.BitString
import Asn1js.Integer
import Asn1js.IntegerParams
import Asn1js.LocalBitStringValueBlockParams
import Asn1js.LocalSimpleStringBlockParams
import Asn1js.PrintableString
import Asn1js.Sequence
import BN
import Buffer
import NodeRSA
import ehn.techiop.hcert.kotlin.chain.asBase64
import ehn.techiop.hcert.kotlin.chain.toByteArray
import ehn.techiop.hcert.kotlin.crypto.CertificateAdapter
import ehn.techiop.hcert.kotlin.crypto.EcPrivKey
import ehn.techiop.hcert.kotlin.crypto.JsEcPrivKey
import ehn.techiop.hcert.kotlin.crypto.JsRsaPrivKey
import ehn.techiop.hcert.kotlin.crypto.PrivKey
import ehn.techiop.hcert.kotlin.crypto.PubKey
import ehn.techiop.hcert.kotlin.trust.ContentType
import hash
import kotlinx.datetime.Clock
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import pkijs.src.AlgorithmIdentifier.AlgorithmIdentifier
import pkijs.src.AttributeTypeAndValue.AttributeTypeAndValue
import pkijs.src.ExtKeyUsage.ExtKeyUsage
import pkijs.src.Extension.Extension
import pkijs.src.PublicKeyInfo.PublicKeyInfo
import pkijs.src.RelativeDistinguishedNames.RelativeDistinguishedNames
import pkijs.src.Time.Time
import tsstdlib.JsonWebKey
import kotlin.js.Date
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration


actual class PkiUtils {
    @Suppress("UNUSED_VARIABLE")
    actual fun selfSignCertificate(
        commonName: String,
        privateKey: PrivKey<*>,
        publicKey: PubKey<*>,
        keySize: Int,
        contentType: List<ContentType>,
        clock: Clock
    ): CertificateAdapter {
        val certificate = pkijs.src.Certificate.Certificate()
        certificate.version = 2
        val serialNumber = Random.nextInt().absoluteValue
        certificate.serialNumber = Integer(object : IntegerParams {
            override var value: Number? = serialNumber
        })
        val cn = PrintableString(object : LocalSimpleStringBlockParams {
            override var value: String? = commonName
        })
        (certificate.subject as RelativeDistinguishedNames).typesAndValues +=
            AttributeTypeAndValue(object {
                val type = "2.5.4.3"
                val value = cn
            })
        (certificate.issuer as RelativeDistinguishedNames).typesAndValues +=
            AttributeTypeAndValue(object {
                val type = "2.5.4.3"
                val value = cn
            })
        (certificate.notBefore as Time).value = Date(clock.now().toEpochMilliseconds())
        (certificate.notAfter as Time).value = Date(clock.now().plus(Duration.days(30)).toEpochMilliseconds())

        val extKeyUsage = ExtKeyUsage().also {
            it.keyPurposes = contentType.map { it.oid }.toTypedArray()
        }
        certificate.extensions = arrayOf(
            Extension(object {
                val extnID = "2.5.29.37"
                val critical = false
                val extnValue = (extKeyUsage.toSchema() as Sequence).toBER()
                val parsedValue = extKeyUsage
            })
        )

        val jwk = publicKey.toPlatformPublicKey() as JsonWebKey
        (certificate.subjectPublicKeyInfo as PublicKeyInfo).fromJSON(jwk)
        val algorithmIdentifier = AlgorithmIdentifier()
        algorithmIdentifier.algorithmId =
            if (privateKey is EcPrivKey) "1.2.840.10045.4.3.2" else "1.2.840.113549.1.1.11"
        certificate.signature = algorithmIdentifier
        certificate.signatureAlgorithm = algorithmIdentifier
        val data = Uint8Array(certificate.encodeTBS().toBER())
        val signatureValue = if (privateKey is EcPrivKey) {
            val sha256 = hash(data)
            val priv = (privateKey as JsEcPrivKey).dValue
            Uint8Array(privateKey.ec.sign(sha256, BN(priv)).toDER()).buffer
        } else {
            privateKey as JsRsaPrivKey
            Uint8Array(NodeRSA().importKey(privateKey.raw as NodeRSA.KeyComponentsPrivate).sign(Buffer(data))).buffer
        }
        certificate.signatureValue = BitString(
            object : LocalBitStringValueBlockParams {
                override var valueHex: ArrayBuffer? = signatureValue
            })
        certificate.tbs = certificate.encodeTBS().toBER()
        val encoded = Buffer((certificate.toSchema(true) as Sequence).toBER()).toByteArray()
        return CertificateAdapter(encoded.asBase64())
    }


}