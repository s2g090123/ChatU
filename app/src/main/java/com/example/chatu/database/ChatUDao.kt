package com.example.chatu.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(message: ChatMessage)

    @Query("Select * from chat_message_table where (from_uid= :me AND to_uid= :other) OR from_uid= :other")
    fun get(me: String, other: String): LiveData<List<ChatMessage>>

    @Query("Select * from chat_message_table")
    fun getAll(): LiveData<List<ChatMessage>>

    @Query("Delete from chat_message_table")
    fun clear()

    @Query("SELECT COUNT(id) FROM chat_message_table")
    fun getRowCount(): LiveData<Long>

    @Query("Update CHAT_MESSAGE_TABLE Set read = 1 where tag = :tag")
    fun update(tag: Long)
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contact: Contact)

    @Query("Select * from contact_table where uid= :uid")
    fun get(uid: String): LiveData<Contact>

    @Query("Select * from contact_table")
    fun getAll(): LiveData<List<Contact>>

    @Query("Delete from contact_table where uid = :uid")
    fun delete(uid: String)
}

@Dao
interface InvitationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(invitation: Invitation)

    @Query("Select * from invitation_table where from_uid= :uid")
    fun get(uid: String): LiveData<Invitation>

    @Query("Select * from invitation_table ORDER BY invite_time")
    fun getAll(): LiveData<List<Invitation>>

    @Delete
    fun delete(invitation: Invitation)

    @Query("Delete from invitation_table")
    fun clear()
}