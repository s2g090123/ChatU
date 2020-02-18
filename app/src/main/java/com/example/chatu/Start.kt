package com.example.chatu


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.*
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.chatu.databinding.FragmentStartBinding
import com.example.chatu.request.RequestMessage
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

/**
 * A simple [Fragment] subclass.
 */
class Start : Fragment() {

    private lateinit var binding: FragmentStartBinding
    private lateinit var mFireBaseDatabaseRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var sharedPref: SharedPreferences
    private lateinit var token: String
    private val RC_SIGN_IN = 1
    private var isRegisted = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mFireBaseDatabase = FirebaseDatabase.getInstance()
        mFireBaseDatabaseRef = mFireBaseDatabase.reference.child("requests")
        binding = FragmentStartBinding.inflate(inflater,container,false)
        sharedPref= activity!!.getSharedPreferences("ChatU",0)

        // 獲取device token，token用於傳遞notification時的地址
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if(!it.isSuccessful)
                throw Exception("getInstanceId failed")
            // Get new Instance ID token
            token =  it.result!!.token
        }

        // 建立身分驗證畫面
        mAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            val user = mAuth.currentUser
            if(user != null) {  // 早已註冊
                if(isRegisted)
                    startToContact(user.displayName)
                else {  // 新註冊
                    val receiver = object: BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            startToContact(user.displayName)
                        }
                    }
                    LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver,IntentFilter("register"))
                }
            }
            else {  // 建立註冊畫面
                isRegisted = false
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false)
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build(),AuthUI.IdpConfig.GoogleBuilder().build()
                    ,AuthUI.IdpConfig.FacebookBuilder().build(),AuthUI.IdpConfig.GitHubBuilder().build()))
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.chatu_test_logo)
                    .build(),RC_SIGN_IN)
            }
        }
        return binding.root
    }

    // 註冊帳號的結果，成功的話則向server獲取uid，並將token存於server中
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN) {
            when(resultCode) {
                RESULT_OK -> sendRegisterRequest(mAuth.currentUser!!.displayName,mAuth.currentUser!!.uid)
                RESULT_CANCELED -> activity!!.onBackPressed()
            }
        }
    }

    // 當獲取uid或早已存在uid，則轉向Contact畫面
    private fun startToContact(name: String?) {
        binding.welcome.text = getString(R.string.welcome_text,name)
        Handler().postDelayed({
            val uid = sharedPref.getString("uid","重新開啟應用程式")
            findNavController().navigate(StartDirections.actionStart2ToFragmentContact(name!!,uid!!))
        },2000L)
    }

    // 發送註冊請求
    private fun sendRegisterRequest(name: String?, uid: String) {
        val data = mapOf(Pair("name",name),Pair("token",token),Pair("uid",uid))
        val request = RequestMessage("register",data)
        mFireBaseDatabaseRef.push().setValue(request)
    }

    override fun onResume() {
        super.onResume()
        mAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        mAuth.removeAuthStateListener(authStateListener)
    }

}
