package com.example.chatu.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "invitation_table")
data class Invitation(
    @ColumnInfo(name = "invite_time")
    val time: String,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "from_uid")
    val from_uid: String,
    @ColumnInfo(name = "to_uid")
    val to_uid: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "answer")
    val answer: String?
): Parcelable {
    @Ignore
    constructor(): this("","","","","")
}