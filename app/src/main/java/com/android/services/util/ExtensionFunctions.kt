package com.android.services.util

import android.util.Patterns
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.services.models.NodeInfo
import com.google.android.material.snackbar.Snackbar
import java.io.File

/** Increments one into a Int Value **/
fun Int.incrementOne(): Int = this + 1

/** Should Request Permission Rationale **/
fun Fragment.shouldShowRequestPermissionRationaleCompat(permissions: Array<String>): Boolean {
    for (i in permissions.indices) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissions[i]))
            return true
    }
    return false
}

/** Show SnackBar for a View **/
fun View.showSnackBar(msg: String, length: Int) {
    showSnackBar(msg, length, null) {}
}

/** Shows snackBar with an action **/
fun View.showSnackBar(
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val mSnackBar = Snackbar.make(this, msg, length)
    if (actionMessage != null) {
        mSnackBar.setAction(actionMessage) {
            action(this)
        }
    }
    mSnackBar.show()
}

/** File Size extensions **/
val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024

/** Retrieves all the necessary Node Info Detail **/
fun AccessibilityNodeInfo?.retrieveNodeInfo(): NodeInfo {
    if (this == null)
        return NodeInfo()
    var id: String? = this.viewIdResourceName
    id?.let {
        val splitIds = it.split("/".toRegex())
        id = if (splitIds.size == 2) {
            splitIds.toTypedArray()[1]
        } else {
            splitIds[0]
        }
    }

    val parentNodeClass = this.parent?.className?.toString() ?: ""
    val packageName = this.packageName?.toString() ?: ""
    val className: String = this.className?.toString() ?: ""
    var nodeInfoText = ""
    if (this.text != null) nodeInfoText =
        this.text?.toString() ?: ""
    val contentDescription = this.contentDescription?.toString() ?: ""
    val childCount: Int = this.childCount ?: 0

    val nodeInfo = NodeInfo()
    nodeInfo.apply {
        this.nodeId = id ?: ""
        this.nodeClassName = className
        this.nodePackageName = packageName
        this.nodeText = nodeInfoText
        this.nodeContentDescription = contentDescription
        this.nodeParent = parentNodeClass
        this.nodeChild = childCount
    }
    return nodeInfo
}

/** formats the service activation code **/
val String.formatCode
    get() = if (!startsWith("#"))
        "##$this".trim() else this.trim()

/** Entered String is a phone number **/
fun String.isValidPhoneNumber(): Boolean =
    !isNullOrEmpty() && Patterns.PHONE.matcher(this).matches()

/** Checks if Text contains any element of the Given List **/
fun List<String>.textContainsListElement(text: String): Boolean {
    this.forEach {
        if (text.contains(it,true))
            return true
    }
    return false
}
/** Checks if Text Equals to any element of the Given List **/
fun List<String>.textEqualToAnyListElement(text: String): Boolean {
    this.forEach {
        if (text.equals(it,true))
            return true
    }
    return false
}
/** Checks if Text StartWith to any element of the Given List **/
fun List<String>.textStartWithAnyListElement(text: String): Boolean {
    this.forEach {
        if (text.startsWith(it,true))
            return true
    }
    return false
}
