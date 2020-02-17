package com.example.chatu.contact

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatu.database.Contact
import com.example.chatu.database.ContactDao
import com.example.chatu.database.Invitation
import com.example.chatu.database.InvitationDao
import com.example.chatu.request.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class ContactViewModel(private val contactDao: ContactDao, private val invitationDao: InvitationDao): ViewModel() {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.reference.child("requests")
    private val userRef = firebaseDatabase.reference.child("user")

    val contacts = contactDao.getAll()

    private val _contactInfo = MutableLiveData<Contact?>()
    val contactInfo: LiveData<Contact?>
        get() = _contactInfo

    fun onContactClicked(contact: Contact) {
        _contactInfo.value = contact
    }
    fun doneNavigating() {
        _contactInfo.value = null
    }

    // 加入好友
    private fun addFriend(invitation: Invitation) {
        coroutineScope.launch {
            val friend = Contact(invitation.from_uid,invitation.name)
            insert(friend)
        }
    }

    // 刪除交友邀請
    private fun deleteInvitation(invitation: Invitation) {
        coroutineScope.launch {
            delete(invitation)
        }
    }


    // 搜尋好友
    private val _searchName = MutableLiveData<String?>()
    val searchName: LiveData<String?>
        get() = _searchName
    private val _searchUid = MutableLiveData<String?>()

    private val _addRequestClicked = MutableLiveData<Boolean?>()
    val addRequestClicked: LiveData<Boolean?>
        get() = _addRequestClicked

    fun onSendFriendRequestClicked() {
        _addRequestClicked.value = true
    }
    fun doneSendFriendRequestClicked() {
        _addRequestClicked.value = null
    }

    // 傳送搜尋使用者request
    fun searchFriend(uid: String, myUid: String) {
        val request = RequestMessage("find",mapOf(Pair("from_uid",myUid), Pair("to_uid",uid)))
        databaseRef.push().setValue(request)
    }
    // 獲取搜尋的結果
    fun getFindFriendResult(uid: String?, name: String?) {
        _searchName.value = name
        _searchUid.value = uid
    }
    // 關閉搜尋
    fun closeSearch() {
        _searchName.value = null
    }

    // 傳送交友邀請
    fun sendAddFriendRequest(myUid: String, myName: String) {
        userRef.child("${_searchUid.value}/token").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val token: String? = p0.getValue(String::class.java)
                coroutineScope.launch {
                    try {
                        val invitation = Invitation(System.currentTimeMillis().toString(),myUid,myName)
                        val request = RequestMessage("invitation",invitation)
                        val response = MessagingApi.retrofitService.sendInvitationRequest(InvitationRequest(request,token!!))
                        response.await()
                    } catch (e: Exception) {
                        Log.i("Chat","error: ${e.message}")
                    }
                }
            }
        })
    }


    // 交友邀請
    val invitations = invitationDao.getAll()
    // 確認邀請
    fun doInvitationAdd(invitation: Invitation, myUid: String, myName: String) {
        userRef.child("${invitation.from_uid}/token").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val token: String? = p0.getValue(String::class.java)
                coroutineScope.launch {
                    try {
                        val data = Invitation(System.currentTimeMillis().toString(),myUid,myName)
                        val request = RequestMessage("reply",data,"Yes")
                        val response = MessagingApi.retrofitService.sendInvitationRequest(InvitationRequest(request,token!!))
                        response.await()
                    } catch (e: Exception) {
                        Log.i("Chat","error: ${e.message}")
                    }
                }
            }
        })
        addFriend(invitation)
        deleteInvitation(invitation)
    }
    // 否決邀請
    fun doInvitationCancel(invitation: Invitation, myUid: String, myName: String) {
        userRef.child("${invitation.from_uid}/token").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val token: String? = p0.getValue(String::class.java)
                coroutineScope.launch {
                    try {
                        val data = Invitation(System.currentTimeMillis().toString(),myUid,myName)
                        val request = RequestMessage("reply",data,"No")
                        val response = MessagingApi.retrofitService.sendInvitationRequest(InvitationRequest(request,token!!))
                        response.await()
                    } catch (e: Exception) {
                        Log.i("Chat","error: ${e.message}")
                    }
                }
            }
        })
        deleteInvitation(invitation)
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

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}