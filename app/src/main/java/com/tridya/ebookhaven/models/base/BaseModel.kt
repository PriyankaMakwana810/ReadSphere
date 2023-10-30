package com.tridya.ebookhaven.models.base

import java.io.Serializable

open class BaseModel<T> : Serializable {
    var data: T? = null
}
