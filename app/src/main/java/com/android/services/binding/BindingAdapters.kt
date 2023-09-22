package com.android.services.binding

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.android.services.R
import com.android.services.models.PermissionCounter
import com.android.services.sealed.Loading
import com.android.services.sealed.NetworkState
import com.android.services.enums.PermissionScreens

object BindingAdapters {

    @BindingAdapter("app:showLoader")
    @JvmStatic
    fun showLoader(progressBar: ProgressBar, networkState: NetworkState?) {
        networkState?.let {
            if (networkState == Loading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter(
        value = ["app:setPermissionScreen", "app:setPermissionCounterList"],
        requireAll = false
    )
    @JvmStatic
    fun setPermissionCounter(
        textView: TextView,
        permissionScreens: PermissionScreens,
        permissionCounterList: List<PermissionCounter>?
    ) {
        permissionCounterList?.let {
            val permissionCounter = permissionCounterList.find {
                it.permission == permissionScreens
            }
            permissionCounter?.let {
                textView.text = "${permissionCounter.permissionNumber}/${permissionCounter.totalPermissions}"
            }
        }
    }

    @BindingAdapter("app:setPermissionText")
    @JvmStatic
    fun setPermissionText(textView: TextView, textList: List<String>) {
        val text = textList[0]
        val startText = textList[1]
        val spannableText = textList[2]
        val spannable: Spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.colorBlue)),
            startText.length,
            (startText + spannableText).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    @BindingAdapter("app:setPermissionInfo")
    @JvmStatic
    fun setPermissionInfo(textView: TextView, text: String) {
        val textList = text.split(",")
        setPermissionText(textView, textList)
    }
}