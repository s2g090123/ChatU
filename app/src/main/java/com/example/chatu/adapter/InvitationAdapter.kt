package com.example.chatu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatu.database.Invitation
import com.example.chatu.databinding.ItemInvitationBinding

class InvitationAdapter(private val addListener: InvitationListener, private val cancelListener: InvitationListener): ListAdapter<Invitation, InvitationAdapter.InvitationViewHolder>(InvitationDiffCallback()) {
    class InvitationDiffCallback: DiffUtil.ItemCallback<Invitation>() {
        override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem.from_uid == newItem.from_uid
        }

        override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem == newItem
        }
    }

    class InvitationViewHolder private constructor(private val binding: ItemInvitationBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Invitation, addListener: InvitationListener, cancelListener: InvitationListener) {
            binding.invitation = item
            binding.addListener = addListener
            binding.cancelListener = cancelListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): InvitationViewHolder {
                val binding = ItemInvitationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return InvitationViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        return InvitationViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        holder.bind(getItem(position), addListener, cancelListener)
    }
}

class InvitationListener(val listener: (invitation: Invitation) -> Unit) {
    fun onClick(invitation: Invitation) {listener(invitation)}
}