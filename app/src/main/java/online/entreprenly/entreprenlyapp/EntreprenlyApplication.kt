package online.entreprenly.entreprenlyapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer

class EntreprenlyApplication : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    /**
     * Image loader for product photos. It identifies the app in the User-Agent: some photo hosts
     * (Wikimedia Commons) reject OkHttp's default one with HTTP 403.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", "EntreprenlyApp/${BuildConfig.VERSION_NAME} (Android)")
                            .build()
                    )
                }
                .build()
        }
        .crossfade(true)
        .build()
}
