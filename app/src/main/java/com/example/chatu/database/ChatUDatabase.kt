package com.example.chatu.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database (entities = [ChatMessage::class,Contact::class,Invitation::class], version = 8, exportSchema = false)
abstract class ChatUDatabase: RoomDatabase() {
    abstract val chatMessageDao: ChatMessageDao
    abstract val contactDao: ContactDao
    abstract val invitationDao: InvitationDao

    companion object {
        @Volatile
        private var INSTANCE: ChatUDatabase? = null

        fun getInstance(context: Context): ChatUDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,ChatUDatabase::class.java,"chat_u_database").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}