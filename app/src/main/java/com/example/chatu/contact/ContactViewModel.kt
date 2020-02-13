package com.example.chatu.contact

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatu.database.Contact
import com.example.chatu.database.ContactDao
import com.example.chatu.database.Invitation
import com.example.chatu.database.InvitationDao
import com.example.chatu.request.RequestMessage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

class ContactViewModel(private val contactDao: ContactDao, private val invitationDao: InvitationDao, private val myUid: String, private val myName: String): ViewModel() {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.reference.child("requests")


    val contacts = contactDao.getAll()

    private val _contactInfo = MutableLiveData<List<String>?>()
    val contactInfo: LiveData<List<String>?>
        get() = _contactInfo

    private fun addFriend(invitation: Invitation) {
        coroutineScope.launch {
            val friend = Contact(invitation.from_uid,invitation.name)
            insert(friend)
        }
    }
    private fun deleteInvitation(invitation: Invitation) {
        coroutineScope.launch {
            delete(invitation)
        }
    }

    private suspend fun insert(contact: Contact) {
        withContext(Dispatchers.IO) {
            contactDao.insert(contact)
        }
    }

    private suspend fun delete(invitation: Invitation) {
        withContext(Dispatchers.IO) {
            invitationDao.delete(invitation)
        }
    }

    fun onContactClicked(name:String, uid: String, myUid: String) {
        _contactInfo.value = listOf(name,uid,myUid)
    }

    fun doneNavigating() {
        _contactInfo.value = null
    }

    // 搜尋好友
    private val _searchName = MutableLiveData<String?>()
    val searchName: LiveData<String?>
        get() = _searchName
    private val _searchUid = MutableLiveData<String?>()

    private val _addRequestClicked = MutableLiveData<Boolean?>()
    val addRequestClicked: LiveData<Boolean?>
        get() = _addRequestClicked

    fun searchFriend(uid: String) {
        val request = RequestMessage("find",mapOf(Pair("from_uid",myUid), Pair("to_uid",uid)))
        databaseRef.push().setValue(request)
    }

    fun getFindFriendResult(uid: String?, name: String?) {
        _searchName.value = name
        _searchUid.value = uid
    }

    fun onSendFriendRequestClicked() {
        _addRequestClicked.value = true
    }

    fun doneSendFriendRequestClicked() {
        _addRequestClicked.value = null
    }

    fun sendFriendRequest() {
        val request = RequestMessage("invitation", Invitation(System.currentTimeMillis().toString(),myUid,_searchUid.value!!,myName,null))
        databaseRef.push().setValue(request)
    }

    fun closeSearch() {
        _searchName.value = null
    }

    // 交友邀請
    val invitations = invitationDao.getAll()

    fun doInvitationAdd(invitation: Invitation) {
        val request = RequestMessage("reply",Invitation(System.currentTimeMillis().toString(),myUid,invitation.from_uid,myName,"Yes"))
        databaseRef.push().setValue(request)
        addFriend(invitation)
        deleteInvitation(invitation)
    }

    fun doInvitationCancel(invitation: Invitation) {
        val request = RequestMessage("reply",Invitation(System.currentTimeMillis().toString(),myUid,invitation.from_uid,myName,"No"))
        databaseRef.push().setValue(request)
        deleteInvitation(invitation)
    }

    fun addInvitation(invitation: Invitation) {
        coroutineScope.launch {
            insertInvitation(invitation)
        }
    }

    private suspend fun insertInvitation(invitation: Invitation) {
        withContext(Dispatchers.IO) {
            invitationDao.insert(invitation)
        }
    }

    // 交友邀請回覆
    fun doInvitationReply(reply: Invitation) {
        if(reply.answer == "Yes") {
            addFriend(reply)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}