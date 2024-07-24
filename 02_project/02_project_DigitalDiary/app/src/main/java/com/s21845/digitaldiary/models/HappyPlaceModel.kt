//package com.s21845.digitaldiary.models
//
//import android.os.Parcel
//import android.os.Parcelable
//
////A Data Model Class for Happy Place details.
//data class HappyPlaceModel(
//    val id: Int,
//    val title: String?,
//    val image: String?,
//    val description: String?,
//    val date: String?,
//    val location: String?,
//    val latitude: Double,
//    val longitude: Double,
//    val audio: String?,
//    val caption: String?
//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readInt(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readDouble(),
//        parcel.readDouble(),
//        parcel.readString(),
//        parcel.readString()
//    ) {
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(id)
//        parcel.writeString(title)
//        parcel.writeString(image)
//        parcel.writeString(description)
//        parcel.writeString(date)
//        parcel.writeString(location)
//        parcel.writeDouble(latitude)
//        parcel.writeDouble(longitude)
//        parcel.writeString(audio)
//        parcel.writeString(caption)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<HappyPlaceModel> {
//        override fun createFromParcel(parcel: Parcel): HappyPlaceModel {
//            return HappyPlaceModel(parcel)
//        }
//
//        override fun newArray(size: Int): Array<HappyPlaceModel?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
package com.s21845.digitaldiary.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class HappyPlaceModel(
    @Exclude var id: String? = null,
    val title: String? = "",
    var image: String? = "",
    val description: String? = "",
    val date: String? = "",
    val location: String? = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val audio: String? = "",
    val caption: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(audio)
        parcel.writeString(caption)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlaceModel> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceModel {
            return HappyPlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}
