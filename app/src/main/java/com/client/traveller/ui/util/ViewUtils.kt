package com.client.traveller.ui.util

import android.content.Context
import android.location.Location
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
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

fun Int.dpToPx(context: Context): Int {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}

fun View.setMargins(context: Context, left: Int, top: Int, right: Int, bottom: Int) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        left.run { params.leftMargin = this.dpToPx(context) }
        top.run { params.topMargin = this.dpToPx(context) }
        right.run { params.rightMargin = this.dpToPx(context) }
        bottom.run { params.bottomMargin = this.dpToPx(context) }
        requestLayout()
    }
}
