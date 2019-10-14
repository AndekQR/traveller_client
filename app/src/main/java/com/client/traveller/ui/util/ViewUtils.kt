package com.client.traveller.ui.util

import android.view.View
import android.widget.RelativeLayout
import com.paulrybitskyi.persistentsearchview.PersistentSearchView


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

fun PersistentSearchView?.showLoading() {
    this?.let {
        it.hideLeftButton()
        it.showProgressBar()
    }
}

fun PersistentSearchView?.hideLoding() {
    this?.let {
        it.hideProgressBar()
        it.showLeftButton()
    }
}
