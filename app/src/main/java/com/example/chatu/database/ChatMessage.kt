package com.example.chatu.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName= "chat_message_table")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = 0L,
    @ColumnInfo(name = "from_uid")
    val from_uid: String,
    @ColumnInfo(name = "to_uid")
    val to_uid: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "chat_content")
    val content: String,
    @ColumnInfo(name = "send_time")
    val time: String,
    @ColumnInfo(name = "read")
    val read: Boolean = false
) {
    @Ignore
    constructor() : this(null,"","","","","",false)
}

