package com.example.chatu.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class Contact(
    @PrimaryKey(autoGenerate = false)
    val uid: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "unread_message")
    val unread: Int = 0
)