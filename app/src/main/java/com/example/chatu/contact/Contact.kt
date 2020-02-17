package com.example.chatu.contact


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatu.R
import com.example.chatu.adapter.ContactAdapter
import com.example.chatu.adapter.ContactListener
import com.example.chatu.adapter.InvitationAdapter
import com.example.chatu.adapter.InvitationListener
import com.example.chatu.database.ChatUDatabase
import com.example.chatu.database.Contact
import com.example.chatu.databinding.DialogFindFriendBinding
import com.example.chatu.databinding.DialogInvitationBinding
import com.example.chatu.databinding.FragmentContactBinding


/**
 * A simple [Fragment] subclass.
 */
class Contact : Fragment() {

    private lateinit var viewModel: ContactViewModel
    private lateinit var broadcastManager: LocalBroadcastManager
    private var intentFilter: IntentFilter? = null
    private lateinit var myName: String
    private lateinit var myUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val arguments = ContactArgs.fromBundle(arguments!!)
        myName = arguments.name
        myUid = arguments.uid
        val binding = FragmentContactBinding.inflate(inflater,container,false)
        (activity as AppCompatActivity).setSupportActionBar(binding.contactToolBar)
        binding.contactToolBar.title = "$myName (${myUid})"

        val contactDao = ChatUDatabase.getInstance(requireNotNull(activity).application).contactDao
        val invitationDao = ChatUDatabase.getInstance(requireNotNull(activity).application).invitationDao
        val viewModelFactory = ContactViewModelFactory(contactDao,invitationDao)
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(ContactViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // 建立聯絡人清單
        binding.contactList.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        val adapter = ContactAdapter(ContactListener { name,uid ->
            viewModel.onContactClicked(Contact(uid,name,0))
        })
        viewModel.contactInfo.observe(this, Observer {
            it?.let {
                findNavController().navigate(ContactDirections.actionFragmentContactToChat2(it.name,it.uid))
                viewModel.doneNavigating()
            }
        })

        binding.contactList.adapter = adapter
        viewModel.contacts.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        broadcastManager = LocalBroadcastManager.getInstance(requireNotNull(activity).application)
    }

    override fun onStart() {
        super.onStart()
        // 註冊收到搜尋使用者結果的receiver
        if(intentFilter == null) {
            intentFilter = IntentFilter("Find")
            broadcastManager.registerReceiver(receiver, intentFilter!!)
        }
    }

    override fun onPause() {
        super.onPause()
        if(intentFilter != null)
            broadcastManager.unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.contact_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // 搜尋使用者和確認邀請的dialog
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val dialogBuilder = AlertDialog.Builder(context!!,R.style.AlertDialogCustom)

        when(item?.itemId) {
            R.id.menu_add_friend -> {
                dialogBuilder.setNegativeButton("關閉") { dialog, _ -> run{
                    viewModel.closeSearch()
                    dialog.dismiss()
                }}
                val dialogViewBinding = DialogFindFriendBinding.inflate(LayoutInflater.from(context),
                    null,false)
                dialogBuilder.setView(dialogViewBinding.root)
                dialogViewBinding.lifecycleOwner = this
                dialogViewBinding.viewModel = viewModel
                // 按下搜尋
                dialogViewBinding.searchButton.setOnClickListener {
                    val uid = dialogViewBinding.findFriendEdit.text.toString()
                    if(uid.isNotEmpty()) {
                        dialogViewBinding.findResult.visibility = View.GONE
                        if (uid.length == 8) {
                            dialogViewBinding.findFriendProgressbar.visibility = View.VISIBLE
                            viewModel.searchFriend(uid,myUid)
                        } else {
                            Toast.makeText(context, "輸入錯誤的UID格式", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // 確認搜尋的結果
                viewModel.searchName.observe(this, Observer {
                    dialogViewBinding.findFriendProgressbar.visibility = View.GONE
                    when (it) {
                        "" -> {
                            Toast.makeText(context, "查無此人", Toast.LENGTH_SHORT).show()
                            dialogViewBinding.findResult.visibility = View.GONE
                        }
                        null -> dialogViewBinding.findResult.visibility = View.GONE
                        else -> dialogViewBinding.findResult.visibility = View.VISIBLE
                    }
                })
                // 傳送交友邀請
                viewModel.addRequestClicked.observe(this, Observer {
                    it?.let {
                        viewModel.sendAddFriendRequest(myUid,myName)
                        viewModel.doneSendFriendRequestClicked()
                        Toast.makeText(context,"已傳送交友邀請",Toast.LENGTH_SHORT).show()
                    }
                    dialogViewBinding.findResult.visibility = View.GONE
                })
            }
            R.id.menu_invitations -> {
                dialogBuilder.setNegativeButton("關閉",null)
                val dialogViewBinding = DialogInvitationBinding.inflate(LayoutInflater.from(context),null,false)
                dialogBuilder.setView(dialogViewBinding.root)
                dialogViewBinding.lifecycleOwner = this
                // 建立好友邀請的畫面
                dialogViewBinding.invitationList.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
                val adapter = InvitationAdapter(InvitationListener { viewModel.doInvitationAdd(it,myUid,myName)}
                    ,InvitationListener {viewModel.doInvitationCancel(it,myUid,myName)} )
                dialogViewBinding.invitationList.adapter = adapter
                viewModel.invitations.observe(this, Observer {adapter.submitList(it)})
            }
        }
        dialogBuilder.show()
        return super.onOptionsItemSelected(item)
    }

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "Find")
                viewModel.getFindFriendResult(intent.getStringExtra("uid"),intent.getStringExtra("name"))
        }
    }
}
