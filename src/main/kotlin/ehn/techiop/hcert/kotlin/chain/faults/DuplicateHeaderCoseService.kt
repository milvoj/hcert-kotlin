package ehn.techiop.hcert.kotlin.chain.faults

import COSE.Attribute
import COSE.Sign1Message
import ehn.techiop.hcert.kotlin.chain.CryptoService
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCoseService

/**
 * Puts header entries into the unprotected *and* protected COSE header.
 *
 * Actually, this conforms to the specification, but we'll prefer to put the entries into the protected COSE header.
 */
class DuplicateHeaderCoseService(private val cryptoService: CryptoService) : DefaultCoseService(cryptoService) {

    override fun encode(input: ByteArray): ByteArray {
        return Sign1Message().also {
            it.SetContent(input)
            for (header in cryptoService.getCborHeaders()) {
                it.addAttribute(header.first, header.second, Attribute.PROTECTED)
                it.addAttribute(header.first, header.second, Attribute.UNPROTECTED)
            }
            it.sign(cryptoService.getCborSigningKey())
        }.EncodeToBytes()
    }

}