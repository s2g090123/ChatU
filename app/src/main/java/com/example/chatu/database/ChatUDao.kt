package com.example.chatu.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(message: ChatMessage)

    @Query("Select * from chat_message_table where (from_uid= :me AND to_uid= :other) OR from_uid= :other Order by send_time")
    fun get(me: String, other: String): LiveData<List<ChatMessage>>

    @Query("Select * from chat_message_table Order by send_time")
    fun getAll(): LiveData<List<ChatMessage>>

    @Query("Delete from chat_message_table")
    fun clear()

    @Query("SELECT COUNT(id) FROM chat_message_table")
    fun getRowCount(): LiveData<Long>

    @Query("Update chat_message_table Set read = 1 where tag = :tag AND from_uid = :uid")
    fun update(tag: Long, uid: String)
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

    @Query("Update contact_table Set unread_message = unread_message + 1 where uid = :uid")
    fun update(uid: String)

    @Query("Update contact_table Set unread_message = 0 where uid = :uid")
    fun reset(uid: String)
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