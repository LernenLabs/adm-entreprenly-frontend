package online.entreprenly.entreprenlyapp.profile

import java.io.ByteArrayInputStream
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.PhoneNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileInputTest {
    @Test
    fun phoneAcceptsInternationalPrefixAndRejectsInvalidCharacters() {
        assertEquals("+51987654321", PhoneNumber.parse(" +51987654321 ")?.value)
        assertEquals("987654321", PhoneNumber.parse("987654321")?.value)
        assertNull(PhoneNumber.parse("123456"))
        assertNull(PhoneNumber.parse("51987654abc"))
        assertNull(PhoneNumber.parse("51+987654321"))
        assertNull(PhoneNumber.parse("1234567890123456"))
    }

    @Test
    fun avatarReadEnforcesFiveMiBLimitEvenWithoutFileMetadata() {
        val limit = 5 * 1024 * 1024
        assertEquals(limit, AvatarImage.readBytes(ByteArrayInputStream(ByteArray(limit))).size)
        assertThrows(AvatarImage.TooLarge::class.java) {
            AvatarImage.readBytes(ByteArrayInputStream(ByteArray(limit + 1)))
        }
    }
}
