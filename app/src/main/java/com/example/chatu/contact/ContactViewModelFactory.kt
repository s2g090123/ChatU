package com.example.chatu.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatu.database.ContactDao
import com.example.chatu.database.InvitationDao

class ContactViewModelFactory(private val contactDao: ContactDao, private val invitationDao: InvitationDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            return ContactViewModel(contactDao,invitationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}