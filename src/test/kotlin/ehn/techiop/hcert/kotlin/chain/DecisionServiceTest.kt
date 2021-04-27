package ehn.techiop.hcert.kotlin.chain

import ehn.techiop.hcert.kotlin.trust.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class DecisionServiceTest {

    val decisionService = DecisionService()

    @Test
    fun good() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun goodContentTest() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.TEST)
            certificateValidContent = mutableListOf(ContentType.TEST)
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun goodContentVaccination() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.VACCINATION)
            certificateValidContent = mutableListOf(ContentType.VACCINATION)
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun goodContentRecovery() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.RECOVERY)
            certificateValidContent = mutableListOf(ContentType.RECOVERY)
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun failContentTest() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.TEST)
            certificateValidContent = mutableListOf()
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failContentVaccination() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.VACCINATION)
            certificateValidContent = mutableListOf(ContentType.RECOVERY, ContentType.TEST)
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failContentRecovery() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            content = mutableListOf(ContentType.RECOVERY)
            certificateValidContent = mutableListOf(ContentType.VACCINATION)
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun goodTimes() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            issuedAt = Instant.now().minus(5, ChronoUnit.SECONDS)
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun goodTimesAhead() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            issuedAt = Instant.now().minus(5, ChronoUnit.SECONDS)
            expirationTime = Instant.now().plus(5, ChronoUnit.SECONDS)
        }

        assertEquals(VerificationDecision.GOOD, decisionService.decide(verificationResult))
    }

    @Test
    fun failBase45() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = false
            coseVerified = true
            cborDecoded = true
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failCose() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = false
            cborDecoded = true
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failCbor() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = false
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failIssued() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            issuedAt = Instant.now().plus(5, ChronoUnit.SECONDS)
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }

    @Test
    fun failExpiration() {
        val verificationResult = VerificationResult().apply {
            base45Decoded = true
            coseVerified = true
            cborDecoded = true
            issuedAt = Instant.now().minus(5, ChronoUnit.SECONDS)
            expirationTime = Instant.now().minus(5, ChronoUnit.SECONDS)
        }

        assertEquals(VerificationDecision.FAIL, decisionService.decide(verificationResult))
    }


}