package com.android.syrenapass.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.syrenapass.domain.entities.FileType

@Entity
//data class для базы данных Room, хранит сведения о файлах, которые должны удаляться
data class FileDbModel(
  @PrimaryKey val uri: String,
  @ColumnInfo val size: Long,
  @ColumnInfo val sizeFormatted: String,
  @ColumnInfo val name: String,
  @ColumnInfo val priority: Int,
  @ColumnInfo val fileType: FileType,
)
