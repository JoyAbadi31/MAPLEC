package com.example.mapproject.models

import android.os.Parcel
import android.os.Parcelable

data class Vendor (
    var nodeKey: String? = null, // Firebase node key
    var vendorAccessKey: String? = null, // Access key to open vendor profile in customer/vendor application.
    var boundToUser: String? = null,
    var isSelling: Boolean? = false,
    val vendorNumber: Int = 0,
    val name: String = "",
    val standNumber: Int = 0,
    val description: String = "",
    val profilePictureURL: String = "",
    val bannerPictureURL: String = "",
    val menuItems: MutableList<MenuItem> = mutableListOf()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        nodeKey = parcel.readString(),
        vendorAccessKey = parcel.readString(),
        boundToUser = parcel.readString(),
        isSelling = parcel.readByte() != 0.toByte(),
        vendorNumber = parcel.readInt(),
        name = parcel.readString() ?: "",
        standNumber = parcel.readInt(),
        description = parcel.readString() ?: "",
        profilePictureURL = parcel.readString() ?: "",
        bannerPictureURL = parcel.readString() ?: "",
        menuItems = parcel.createTypedArrayList(MenuItem.CREATOR) ?: mutableListOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nodeKey)
        parcel.writeString(vendorAccessKey)
        parcel.writeString(boundToUser)
        parcel.writeByte(if (isSelling == true) 1 else 0)
        parcel.writeInt(vendorNumber)
        parcel.writeString(name)
        parcel.writeInt(standNumber)
        parcel.writeString(description)
        parcel.writeString(profilePictureURL)
        parcel.writeString(bannerPictureURL)
        parcel.writeTypedList(menuItems)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Vendor> {
        override fun createFromParcel(parcel: Parcel): Vendor {
            return Vendor(parcel)
        }

        override fun newArray(size: Int): Array<Vendor?> {
            return arrayOfNulls(size)
        }
    }
}
