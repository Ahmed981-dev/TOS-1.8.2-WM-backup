package com.android.services.models

data class NodeInfo(
    var nodeId: String,
    var nodeClassName: String,
    var nodePackageName: String,
    var nodeText: String,
    var nodeContentDescription: String,
    var nodeParent: String,
    var nodeChild: Int
) {
    constructor() : this("", "", "", "", "", "", 0)
}
