package online.entreprenly.entreprenlyapp.iam

import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.PasswordResetLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordResetLinkTest {
    private val token = "5c2f8240-8ea0-43c0-bec6-c8127280af1e"

    @Test
    fun acceptsOnlyTheExpectedResetDestinationAndParameters() {
        val valid = PasswordResetLink.parse("entreprenly-av2://reset-password/42/$token")
        assertEquals(42L, valid?.userId)
        assertEquals(token, valid?.token)
        assertNull(PasswordResetLink.parse("https://reset-password/42/$token"))
        assertNull(PasswordResetLink.parse("entreprenly-av2://reset-password.evil.test/42/$token"))
        assertNull(PasswordResetLink.parse("entreprenly-av2://reset-password/0/$token"))
        assertNull(PasswordResetLink.parse("entreprenly-av2://reset-password/42/not-a-token"))
        assertNull(PasswordResetLink.parse("entreprenly-av2://reset-password/42/$token/extra"))
    }
}
