package com.tridya.ebookhaven.models.base

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel(private val mContext: Context) : ViewModel() {
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<BaseErrorModel> = MutableLiveData()

    init {
        errorLiveData.observeForever {
            it.message?.let { it1 ->
                Toast.makeText(mContext, "Servier Side Error!!", Toast.LENGTH_SHORT).show()
//                errorDialog.setMessage(it1).show()
            }
//                errorDialog.setMessage("Something went wrong. Please try again later.").show()
            isLoading.value = false
            isRefreshing.value = false
        }

    }
}