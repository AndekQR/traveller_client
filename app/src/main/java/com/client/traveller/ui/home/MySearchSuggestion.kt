//package com.client.traveller.ui.home
//
//import android.os.Parcel
//import android.os.Parcelable
//import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
//
////TODO można przenieść wyżej (np. jako provider)
//class MySearchSuggestion(private var name: String) : SearchSuggestion {
//
//    constructor(parcel: Parcel) : this(parcel.readString()!!)
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    override fun writeToParcel(dest: Parcel?, flags: Int) {
//        dest?.writeString(name)
//    }
//
//    override fun getBody(): String {
//        return name
//    }
//
////    companion object CREATOR : Parcelable.Creator<MySearchSuggestion> {
////        override fun createFromParcel(parcel: Parcel): MySearchSuggestion {
////            return MySearchSuggestion(parcel.readString()!!)
////        }
////
////        override fun newArray(size: Int): Array<MySearchSuggestion?> {
////            return arrayOfNulls(size)
////        }
////    }
//
//    companion object {
//        @JvmField
//        val CREATOR: Parcelable.Creator<MySearchSuggestion?> = object: Parcelable.Creator<MySearchSuggestion?>{
//            override fun createFromParcel(p0: Parcel): MySearchSuggestion? {
//            return MySearchSuggestion(p0)
//            }
//
//            override fun newArray(p0: Int): Array<MySearchSuggestion?> {
//                return arrayOfNulls(p0)
//            }
//
//        }
//    }
//}