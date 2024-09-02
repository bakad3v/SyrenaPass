package com.android.syrenapass.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileDbModel (
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val main: Boolean,
    @ColumnInfo val toDelete: Boolean=false
)