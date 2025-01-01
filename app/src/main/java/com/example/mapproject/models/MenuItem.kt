package com.example.mapproject.models

import android.os.Parcel
import android.os.Parcelable

data class MenuItem(
    var nodeKey: String = "",
    var itemNumber: Int = 0,
    var itemName: String = "",
    var itemDescription: String = "",
    var itemStock: Int = 0,
    var itemPrice: Int = 0,
    var itemPictureURL: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        nodeKey = parcel.readString() ?: "",
        itemNumber = parcel.readInt(),
        itemName = parcel.readString() ?: "",
        itemDescription = parcel.readString() ?: "",
        itemStock = parcel.readInt(),
        itemPrice = parcel.readInt(),
        itemPictureURL = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nodeKey)
        parcel.writeInt(itemNumber)
        parcel.writeString(itemName)
        parcel.writeString(itemDescription)
        parcel.writeInt(itemStock)
        parcel.writeInt(itemPrice)
        parcel.writeString(itemPictureURL)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MenuItem> {
        override fun createFromParcel(parcel: Parcel): MenuItem {
            return MenuItem(parcel)
        }

        override fun newArray(size: Int): Array<MenuItem?> {
            return arrayOfNulls(size)
        }
    }
}
