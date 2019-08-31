package com.client.traveller.ui.util

import android.view.View
import android.widget.ProgressBar


fun ProgressBar.show(){
    Coroutines.main{
        visibility = View.VISIBLE
    }
}

fun ProgressBar.hide(){
    Coroutines.main{
        visibility = View.GONE
    }
}