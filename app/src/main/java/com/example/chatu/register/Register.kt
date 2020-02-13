package com.example.chatu.register


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.chatu.databinding.FragmentRegisterBinding

/**
 * A simple [Fragment] subclass.
 */
class Register : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentRegisterBinding.inflate(inflater,container,false)
        val viewModelFactory = RegisterViewModelFactory(requireNotNull(this.activity).application)
        val viewModel =ViewModelProviders.of(this,viewModelFactory).get(RegisterViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.navigateToContact.observe(this, Observer {
            it?.let {
                val name = binding.registerEdit.text.toString()
                if(name.isNotEmpty()) {
                    val uid = viewModel.setPreferenceName(name)
                    findNavController().navigate(RegisterDirections.actionFragmentRegisterToFragmentContact(name,uid))
                }
                viewModel.doneNavigating()
            }
        })
        return binding.root
    }


}
