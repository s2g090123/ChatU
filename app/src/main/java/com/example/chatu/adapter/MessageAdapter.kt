package com.example.chatu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatu.database.ChatMessage
import com.example.chatu.databinding.ItemChatContentBinding
import com.example.chatu.databinding.ItemChatContentMeBinding
import com.example.chatu.databinding.ItemChatStickerBinding
import com.example.chatu.databinding.ItemChatStickerMeBinding
import java.lang.Exception

private const val ITEM_TEXT_TYPE = 0
private const val ITEM_TEXT_ME_TYPE = 1
private const val ITEM_STICKER_TYPE = 2
private const val ITEM_STICKER_ME_TYPE = 3

class MessageAdapter(private val myUid: String): ListAdapter<ChatMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    class MessageDiffCallback: DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        when(item.type) {
            "0" -> {
                return if(item.from_uid == myUid) {
                    ITEM_TEXT_ME_TYPE
                } else {
                    ITEM_TEXT_TYPE
                }
            }
            "1" -> {
                return if(item.from_uid == myUid) {
                    ITEM_STICKER_ME_TYPE
                } else {
                    ITEM_STICKER_TYPE
                }
            }
            else -> throw Exception("Unknown Message Type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_TEXT_TYPE -> TextViewHolder.from(parent)
            ITEM_TEXT_ME_TYPE -> TextMeViewHolder.from(parent)
            ITEM_STICKER_TYPE -> StickerViewHolder.from(parent)
            ITEM_STICKER_ME_TYPE -> StickerMeViewHolder.from(parent)
            else -> throw Exception("Unknown Message Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is TextViewHolder -> holder.bind(getItem(position))
            is TextMeViewHolder -> holder.bind(getItem(position))
            is StickerViewHolder -> holder.bind(getItem(position))
            is StickerMeViewHolder -> holder.bind(getItem(position))
        }
    }

    class TextViewHolder private constructor(val binding: ItemChatContentBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            binding.message = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val binding = ItemChatContentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return TextViewHolder(binding)
            }
        }
    }

    class TextMeViewHolder private constructor(val binding: ItemChatContentMeBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            binding.message = item
            if(item.read) {
                binding.isReadText.visibility = View.VISIBLE
            }
            else {
                binding.isReadText.visibility = View.GONE
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextMeViewHolder {
                val binding = ItemChatContentMeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return TextMeViewHolder(binding)
            }
        }
    }

    class StickerViewHolder private constructor(val binding: ItemChatStickerBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            binding.message = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): StickerViewHolder {
                val binding = ItemChatStickerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return StickerViewHolder(binding)
            }
        }
    }

    class StickerMeViewHolder private constructor(val binding: ItemChatStickerMeBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            binding.message = item
            if(item.read) {
                binding.isReadText.visibility = View.VISIBLE
            }
            else {
                binding.isReadText.visibility = View.GONE
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): StickerMeViewHolder {
                val binding = ItemChatStickerMeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return StickerMeViewHolder(binding)
            }
        }
    }

}
