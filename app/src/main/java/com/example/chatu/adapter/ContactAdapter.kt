package com.example.chatu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatu.database.Contact
import com.example.chatu.databinding.ItemContactBinding

class ContactAdapter(private val clickListener: ContactListener): ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {
    class ContactDiffCallback: DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.uid == newItem.uid
        }
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }

    }

    class ContactViewHolder private constructor(private val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact, listener: ContactListener) {
            binding.contact = contact
            binding.contactListener = listener
            if(contact.unread == 0)
                binding.unreadText.visibility = View.GONE
            else
                binding.unreadText.visibility = View.VISIBLE
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ContactViewHolder {
                val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return ContactViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }
}

class ContactListener(val clickListener: (name: String, uid: String) -> Unit) {
    fun onClick(contact: Contact) {clickListener(contact.name,contact.uid)}
}