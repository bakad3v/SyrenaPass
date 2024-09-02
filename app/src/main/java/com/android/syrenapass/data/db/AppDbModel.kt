package com.android.syrenapass.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppDbModel(
    @PrimaryKey val packageName: String,
    @ColumnInfo val appName: String,
    @ColumnInfo val system: Boolean,
    @ColumnInfo val enabled: Boolean,
    @ColumnInfo val toDelete: Boolean,
    @ColumnInfo val toHide: Boolean,
    @ColumnInfo val toClearData: Boolean
)