package com.example.chatu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatu.databinding.ItemStickerBinding

class StickerAdapter(private val clickListener: StickerListener): ListAdapter<Int, StickerAdapter.StickerListViewHolder>(StickerDiffCallback()) {
    class StickerDiffCallback: DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    class StickerListViewHolder private constructor(private val binding: ItemStickerBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Int, listener: StickerListener) {
            binding.sticker.setImageResource(item)
            binding.sticker.setOnClickListener {
                listener.onClick(item)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): StickerListViewHolder {
                val binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return StickerListViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerListViewHolder {
        return StickerListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: StickerListViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }
}

class StickerListener(val listener: (res: Int) -> Unit) {
    fun onClick(res: Int) {listener(res)}
}