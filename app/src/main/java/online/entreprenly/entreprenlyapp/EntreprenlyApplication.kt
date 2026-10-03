package online.entreprenly.entreprenlyapp

import android.app.Application
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer

class EntreprenlyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
