package online.entreprenly.entreprenlyapp.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
import org.junit.Test

class ApiCallTest {
    @Test(expected = CancellationException::class)
    fun cancellationIsPropagatedWhenTheSessionChanges() {
        runBlocking {
            safeApiCall<String, Unit>(call = { throw CancellationException("Session changed") }, transform = {})
        }
    }
}
