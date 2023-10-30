package com.tridya.ebookhaven.models.base

data class BaseErrorModel(
    var message: String? = null,
    var code: Int? = null
)
