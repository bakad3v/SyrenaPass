package com.android.syrenapass

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.android.syrenapass.data.db.MainDataBase
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteException
import javax.inject.Inject

@HiltAndroidApp
class SyrenaApp: Application(), Configuration.Provider  {


  companion object {
    private var INSTANCE: MainDataBase? = null
    private val LOCK = Any()
    fun getDatabase(): MainDataBase? = INSTANCE

    /**
     * Function for creating or opening singleton encrypted database and checking password
     */
    fun createDatabase(context: Context, password: CharArray): MainDataBase? {
      INSTANCE?.let{
        return INSTANCE
      }
      synchronized(LOCK) {
        INSTANCE?.let {
          return INSTANCE
        }
        val db = MainDataBase.create(context,password)
        try {
          db.openHelper.readableDatabase
          INSTANCE = db
          return db
        } catch (e: SQLiteException){
          return null
        }
      }
    }
  }

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface HiltWorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
  }

  @Inject
  lateinit var hiltWorkerFactory: HiltWorkerFactory

  override val workManagerConfiguration =
    Configuration.Builder()
      .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
      .build()

}
