package com.example.parkedcarfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel



class LocationViewModel : ViewModel() {
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    init {
        _location.value = "Select a location to park your car"
    }

//    fun setParkedLocation(parkedLoc: String) {
//        _location.value = (_location.value ?: "") + parkedLoc
//    }
fun setParkedLocation(parkedLoc: String) {
    _location.value = parkedLoc
}
}



//class LocationViewModel : ViewModel() {
//    private val _location = MutableLiveData<String>()
////    val location: LiveData<String> get() = _location
//
//    val location: LiveData<String> = _location
//    init{
//        _location.postValue("")
//    }
//
//
//    fun setParkedLocation(parkedLoc: String) {
////        _location.value = parkedLoc
//
//        _location.postValue((_location.value ?: "") + parkedLoc)
//    }
//}



////class LocationViewModel : ViewModel() {
////    var location: LatLng? = null
////
////    fun setParkedLocation(parkedLoc: LatLng): LatLng {
////        location = parkedLoc
////        return location as LatLng
////    }
////}
//
//
//
//class LocationViewModel : ViewModel() {
//    private val _location = MutableLiveData<String>()
//    val location: LiveData<String> get() = _location
//
//    fun setParkedLocation(parkedLoc: String) {
//        _location.value = parkedLoc
//    }
//}