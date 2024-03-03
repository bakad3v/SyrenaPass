package com.android.syrenapass.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogDbModel (
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo val date: Long,
  @ColumnInfo val day: Long,
  @ColumnInfo val entry: String
  )
