package com.example.chatu.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invitation_table")
data class Invitation(
    @ColumnInfo(name = "invite_time")
    val time: String,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "from_uid")
    val from_uid: String,
    @ColumnInfo(name = "name")
    val name: String
)