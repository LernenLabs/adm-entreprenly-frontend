package online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration

import java.util.Locale
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Builds the Retrofit client; attaches the Bearer token when a session exists and
 * sends Accept-Language so backend messages match the device language. */
class RetrofitFactory(
    baseUrl: String,
    private val tokenProvider: () -> String?,
    debug: Boolean
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val token = tokenProvider()
            val builder = chain.request().newBuilder()
                .header("Accept-Language", Locale.getDefault().toLanguageTag())
            if (token != null) builder.header("Authorization", "Bearer $token")
            chain.proceed(builder.build())
        })
        .apply {
            if (debug) addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
