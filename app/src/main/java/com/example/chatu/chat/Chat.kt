package com.example.chatu.chat


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatu.Sticker
import com.example.chatu.adapter.MessageAdapter
import com.example.chatu.adapter.StickerAdapter
import com.example.chatu.adapter.StickerListener
import com.example.chatu.chat.ChatArgs
import com.example.chatu.database.ChatMessage
import com.example.chatu.database.ChatUDatabase
import com.example.chatu.databinding.FragmentChatBinding
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class Chat : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var DatabaseRef: DatabaseReference
    private var lastValueEventListener: ChildEventListener? = null

    private lateinit var viewModel: ChatViewModel
    private lateinit var uid: String
    private lateinit var myUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseDatabase = FirebaseDatabase.getInstance()
        DatabaseRef = firebaseDatabase.reference
        val name = ChatArgs.fromBundle(arguments!!).name
        uid = ChatArgs.fromBundle(arguments!!).uid
        myUid = ChatArgs.fromBundle(arguments!!).myUid
        val binding = FragmentChatBinding.inflate(inflater,container,false)
        (activity as AppCompatActivity).setSupportActionBar(binding.chatToolBar)
        binding.chatToolBar.title = "$name (${uid})"

        val messageDao = ChatUDatabase.getInstance(requireNotNull(activity!!)).chatMessageDao
        val viewModelFactory = ChatViewModelFactory(messageDao,myUid,uid)
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(ChatViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = MessageAdapter(myUid)
        binding.chatContent.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        binding.chatContent.adapter = adapter
        viewModel.messages.observe(this, Observer {
            adapter.submitList(it)
            binding.chatContent.smoothScrollToPosition(if(adapter.itemCount > 0) adapter.itemCount else 0)
        })

        viewModel.sendClicked.observe(this, Observer {
            it?.let {
                val text = binding.chatEdit.text.toString()
                if(text.isNotEmpty()) {
                    val message = ChatMessage(null,myUid,uid,"0",text,System.currentTimeMillis().toString())
                    viewModel.sendMessage(message)
                }
                binding.chatEdit.setText("")
                viewModel.doneSendClicked()
            }
        })

        val stickerAdapter = StickerAdapter(StickerListener {
            val message = ChatMessage(null,myUid,uid,"1",it.toString(),System.currentTimeMillis().toString())
            viewModel.sendMessage(message)
            viewModel.doneStickerClicked()
        })
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
        //viewModel.clearMessage()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private fun attachDatabaseLastListener() {
        if(lastValueEventListener == null) {
            lastValueEventListener = object: ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val message = p0.getValue(ChatMessage::class.java)
                    if(message!!.from_uid == uid && message.to_uid == myUid) {
                        if(!message.read) {
                            p0.ref.child("read").setValue(true)
                            viewModel.getMessage(message)
                        }
                    }
                }
                override fun onChildRemoved(p0: DataSnapshot) {}
            }
            DatabaseRef.child("chats").addChildEventListener(lastValueEventListener as ChildEventListener)
        }
    }

    override fun onStart() {
        super.onStart()
        attachDatabaseLastListener()
    }

    override fun onPause() {
        super.onPause()
        if(lastValueEventListener != null) {
            DatabaseRef.child("chats").removeEventListener(lastValueEventListener!!)
            lastValueEventListener = null
        }
    }
}
