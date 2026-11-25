package online.entreprenly.entreprenlyapp.profile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import online.entreprenly.entreprenlyapp.iam.application.internal.commandservices.UserCommandServiceImpl
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.repositories.SessionRepository
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.api.IamApi
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.repositories.UserRepositoryImpl
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileEmailRequestTest {
    @Test
    fun requestingAnEmailChangeSendsTheNewAddressAndKeepsTheCurrentSession() = runBlocking {
        val current = AuthSession(7, "current@example.com", "test-token")
        val sessions = object : SessionRepository {
            override val session = MutableStateFlow<AuthSession?>(current)
            override suspend fun save(session: AuthSession) { this.session.value = session }
            override suspend fun clear() { session.value = null }
        }
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/users/me/email-change-requests", request.url.encodedPath)
            val json = Buffer()
            requireNotNull(request.body).writeTo(json)
            assertEquals("""{"email":"new@example.com"}""", json.readUtf8())
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(202)
                .message("Accepted")
                .body("".toResponseBody())
                .build()
        }.build()
        val api = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IamApi::class.java)
        val service = UserCommandServiceImpl(UserRepositoryImpl(api), sessions)

        val result = service.handle(ChangeEmailCommand(requireNotNull(Email.of("new@example.com"))))

        assertTrue(result is Result.Success)
        assertEquals(current, sessions.session.value)
    }
}
