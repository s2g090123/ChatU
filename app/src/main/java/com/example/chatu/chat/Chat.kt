package com.example.chatu.chat


import android.content.*
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatu.Sticker
import com.example.chatu.adapter.MessageAdapter
import com.example.chatu.adapter.StickerAdapter
import com.example.chatu.adapter.StickerListener
import com.example.chatu.database.ChatMessage
import com.example.chatu.database.ChatUDatabase
import com.example.chatu.databinding.FragmentChatBinding
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class Chat : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private var lastValueEventListener: ChildEventListener? = null

    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var name: String
    private lateinit var uid: String
    private lateinit var myUid: String
    private lateinit var myName: String
    private var count = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val firebaseDatabase = FirebaseDatabase.getInstance()
        databaseRef = firebaseDatabase.getReference("/chats")
        databaseRef.keepSynced(true)

        name = ChatArgs.fromBundle(arguments!!).name
        myName = requireNotNull(activity!!).getSharedPreferences("ChatU",0).getString("name","")!!
        uid = ChatArgs.fromBundle(arguments!!).uid
        myUid = requireNotNull(activity!!).getSharedPreferences("ChatU",0).getString("uid","")!!
        binding = FragmentChatBinding.inflate(inflater,container,false)
        (activity as AppCompatActivity).setSupportActionBar(binding.chatToolBar)
        binding.chatToolBar.title = "$name (${uid})"

        val messageDao = ChatUDatabase.getInstance(requireNotNull(activity!!)).chatMessageDao
        val contactDao = ChatUDatabase.getInstance(requireNotNull(activity!!)).contactDao
        val viewModelFactory = ChatViewModelFactory(messageDao,contactDao)
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(ChatViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.getAllMessages(myUid,uid)

        // 建立聊天內容
        val adapter = MessageAdapter(myUid)
        binding.chatContent.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        binding.chatContent.adapter = adapter
        viewModel.messages.observe(this, Observer {
            adapter.submitList(it)
            binding.chatContent.smoothScrollToPosition(if(adapter.itemCount > 0) adapter.itemCount else 0)
        })

        // 傳送貼圖
        val stickerAdapter = StickerAdapter(StickerListener {
            val message = ChatMessage(null,count,myUid,uid,"1",it.toString(),System.currentTimeMillis().toString(),false)
            viewModel.sendMessage(message,myName)
            viewModel.doneStickerClicked()
        })

        // 建立貼圖內容
        binding.stickerContent.layoutManager = GridLayoutManager(context,4)
        binding.stickerContent.adapter = stickerAdapter
        stickerAdapter.submitList(Sticker.stickerList)
        viewModel.stickerClicked.observe(this, Observer {
            if(it != null) {
                binding.stickerContent.visibility = View.VISIBLE
                binding.stickerCancel.visibility = View.VISIBLE
            }
            else {
                binding.stickerContent.visibility = View.GONE
                binding.stickerCancel.visibility = View.GONE
            }
        })

        // count是用來記錄，local db中所有訊息的數量，標記每個message的tag用的，tag主要是用來之後更新未讀/已讀的訊息
        viewModel.messageCount.observe(this, Observer {
            count = it
        })

        // 傳送訊息
        viewModel.sendClicked.observe(this, Observer {
            it?.let {
                val text = binding.chatEdit.text.toString()
                if(text.isNotEmpty()) {
                    val message = ChatMessage(null,count,myUid,uid,"0",text,System.currentTimeMillis().toString(),false)
                    viewModel.sendMessage(message,myName)
                }
                binding.chatEdit.setText("")
                viewModel.doneSendClicked()
            }
        })
        binding.chatEdit.setOnKeyListener { _, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                viewModel.onSendClicked()
                return@setOnKeyListener true
            }
            false
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    // 載入firebase db的訊息以及更新未讀訊息
    private fun attachDatabaseLastListener() {
        if(lastValueEventListener == null) {
            lastValueEventListener = object: ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    val message = p0.getValue(ChatMessage::class.java)
                    if(message!!.from_uid == myUid && message.to_uid == uid && message.read) {
                        viewModel.updateMessage(myUid,message.tag)
                    }
                }
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val message = p0.getValue(ChatMessage::class.java)
                    if(message!!.from_uid == uid && message.to_uid == myUid) {
                        if(!message.read) {
                            p0.ref.updateChildren(mapOf(Pair("read",true)))
                            viewModel.getMessage(message)
                        }
                    }
                    if(message.from_uid == myUid && message.to_uid == uid) {
                        if(message.read) {
                            viewModel.updateMessage(myUid,message.tag)
                        }
                    }
                }
                override fun onChildRemoved(p0: DataSnapshot) {}
            }
            databaseRef.orderByChild("time").addChildEventListener(lastValueEventListener as ChildEventListener)
        }
    }

    // 更新現在和誰在對話，防止更新未讀訊息的數量以及跳出通知
    private fun setCurrentOtherUser(uid: String?) {
        val sharedPref: SharedPreferences = requireNotNull(activity!!).applicationContext.getSharedPreferences("ChatU",0)
        sharedPref.edit().putString("otherUser", uid).apply()
    }

    override fun onResume() {
        super.onResume()
        attachDatabaseLastListener()
        setCurrentOtherUser(uid)
        viewModel.resetUnread(uid)
    }

    override fun onPause() {
        super.onPause()
        if(lastValueEventListener != null) {
            databaseRef.removeEventListener(lastValueEventListener!!)
            lastValueEventListener = null
        }
        setCurrentOtherUser(null)
    }

}
