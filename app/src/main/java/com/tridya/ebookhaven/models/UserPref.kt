package com.tridya.ebookhaven.models

import java.io.Serializable

data class UserPref(
    var bookTitle: String? = null,
    var currentPage: Int? = null,
    var firstPage: String? = null,
    var bookSize: Int? = null,
    var backgroundColor: String? = null,
    var brightness: String? = null,
    var fontSize: String? = null,
    var fontColor: String? = null,
    var font: String? = null,
    var readingModeSwipe: Boolean? = false,
    var isAutomaticScroll: Boolean? = false,
    var isScreenRotationLock: Boolean? = true,
    var isBrowsingByMargins: Boolean? = false,
    var isBookLock: Boolean? = null,
    var bookPassword: String? = null,
    var lineSpacing: String? = null,
    var wordSpacing: String? = null,
    var sideSpacing: String? = null
) : Serializable