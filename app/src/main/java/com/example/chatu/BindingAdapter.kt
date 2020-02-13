package com.example.chatu

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.chatu.database.ChatUDatabase
import java.text.SimpleDateFormat

@BindingAdapter("contentName")
fun setContactName(textView: TextView, uid: String) {
    val dao = ChatUDatabase.getInstance(textView.context).contactDao
    textView.text = dao.get(uid).value?.name
}

@BindingAdapter("contentTime")
fun setContentTime(textView: TextView, time: String) {
    textView.text = SimpleDateFormat("HH:mm")
        .format(time.toLong())
}

@BindingAdapter("contentImage")
fun setContentImage(imageView: ImageView, idString: String) {
    imageView.setImageResource(idString.toInt())
}

@BindingAdapter("stickerImage")
fun setStickerImage(imageView: ImageView, id: Int) {
    imageView.setImageResource(id)
}