package com.android.syrenapass.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [FileDbModel::class, LogDbModel::class, AppDbModel::class, ProfileDbModel::class], version = 1)
abstract class MainDataBase : RoomDatabase() {
  abstract fun myFileDao(): FileDao
  abstract fun myLogDao(): LogDao
  abstract fun myAppDao(): AppDao
  abstract fun myProfileDao(): ProfileDAO

  companion object {

    private const val DB_NAME = "main_db"

    /**
     * Function to open or create encrypted database
     */
    fun create(context: Context, password: CharArray): MainDataBase {
      //Log
      val supportFactory = SupportFactory(SQLiteDatabase.getBytes(password))
      return Room.databaseBuilder(
        context,
        MainDataBase::class.java,
        DB_NAME
      ).openHelperFactory(supportFactory) .build()
    }

  }
}
