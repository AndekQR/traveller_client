package com.client.traveller.ui.util

import android.view.View
import android.widget.RelativeLayout


fun RelativeLayout?.showProgressBar() {
    Coroutines.main {
        this?.visibility = View.VISIBLE
    }
}


fun RelativeLayout?.hideProgressBar() {
    Coroutines.main {
        this?.visibility = View.GONE
    }
}
