package com.example.chatu


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment.findNavController
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

    lateinit var binding: FragmentStartBinding
    lateinit var mFireBaseDatabase: FirebaseDatabase
    lateinit var mFireBaseDatabaseRef: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var authStateListener: FirebaseAuth.AuthStateListener
    lateinit var sharedPref: SharedPreferences
    lateinit var token: String
    val RC_SIGN_IN = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mFireBaseDatabase = FirebaseDatabase.getInstance()
        mFireBaseDatabaseRef = mFireBaseDatabase.reference.child("requests")
        binding = FragmentStartBinding.inflate(inflater,container,false)
        sharedPref= activity!!.getSharedPreferences("ChatU",0)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if(!it.isSuccessful) {
                Log.w("Start", "getInstanceId failed")
                throw Exception("getInstanceId failed")
            }

            // Get new Instance ID token
            token =  it.result!!.token
        }

        mAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener {
            val user = mAuth.currentUser
            if(user != null) {
                startToContact(user.displayName)
            }
            else {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false)
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build(),AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(),RC_SIGN_IN)
            }
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            RC_SIGN_IN -> {
                when(resultCode) {
                    RESULT_OK -> register(mAuth.currentUser!!.displayName)
                    RESULT_CANCELED -> activity!!.onBackPressed()
                }
            }
        }
    }

    private fun startToContact(name: String?) {
        binding.welcome.text = getString(R.string.welcome_text,name)
        Handler().postDelayed({
            val uid = sharedPref.getString("uid","重新開啟應用程式")
            findNavController(this).navigate(StartDirections.actionStart2ToFragmentContact(name!!,uid))
        },5000)
    }

    private fun register(name: String?) {
        val registerMap = mapOf(Pair("name",name),Pair("token",token))
        val request = RequestMessage("register",registerMap)
        mFireBaseDatabaseRef.push().setValue(request)
    }

    override fun onResume() {
        super.onResume()
        mAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        if(authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener)
        }
    }
}
