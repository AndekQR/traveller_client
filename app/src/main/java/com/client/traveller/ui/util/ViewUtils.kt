package com.client.traveller.ui.util

import android.view.View
import android.widget.RelativeLayout


fun RelativeLayout.showProgressBar() {
    Coroutines.main {
        visibility = View.VISIBLE
    }
}


fun RelativeLayout.hideProgressBar() {
    Coroutines.main {
        visibility = View.GONE
    }
}
