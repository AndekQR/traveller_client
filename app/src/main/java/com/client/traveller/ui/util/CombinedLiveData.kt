package com.client.traveller.ui.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.client.traveller.data.db.entities.Messeage
import kotlinx.coroutines.flow.combine

/**
 * T musi być typu List<*>
 */
class CombinedLiveData<T>(source1: LiveData<T>, source2: LiveData<T>, private val combine: (data1: T?, data2: T?) -> T): MediatorLiveData<T>() {

    private var data1: T? = null
    private var data2: T? = null

    init {
        super.addSource(source1) {source ->
            source?.let {
                data1 = it
                value = combine(data1, data2)
            }
        }
        super.addSource(source2) {source ->
            source?.let {
                data1 = it
                value = this.combine(data1, data2)
            }
        }
    }


}