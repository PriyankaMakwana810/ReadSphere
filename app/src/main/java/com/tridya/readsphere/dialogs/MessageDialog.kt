package com.tridya.readsphere.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.tridya.readsphere.R
import com.tridya.readsphere.databinding.CommonDialogMessageBinding

class MessageDialog(context: Context?) : AlertDialog(context!!, R.style.DialogWithAnimation),
    View.OnClickListener {
    private var cancelable = false
    private var title: String? = null
    private var message: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var onPositiveButtonClick: DialogInterface.OnClickListener? = null
    private var onNegativeButtonClick: DialogInterface.OnClickListener? = null
    fun setTitle(title: String?): MessageDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String?): MessageDialog {
        this.message = message
        return this
    }

    fun cancelable(cancelable: Boolean): MessageDialog {
        this.cancelable = cancelable
        return this
    }

    fun setPositiveButton(
        text: String?,
        listener: DialogInterface.OnClickListener?,
    ): MessageDialog {
        positiveButtonText = text
        onPositiveButtonClick = listener
        return this
    }

    fun setNegativeButton(
        text: String?,
        listener: DialogInterface.OnClickListener?,
    ): MessageDialog {
        negativeButtonText = text
        onNegativeButtonClick = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinder: CommonDialogMessageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.common_dialog_message, null, false
        )
        setContentView(mBinder.root)
        setCanceledOnTouchOutside(cancelable)
        setCancelable(cancelable)
        mBinder.tvTitle.visibility = if (title != null) View.VISIBLE else View.GONE
        if (title != null) mBinder.tvTitle.text = title
        mBinder.tvMessage.visibility = if (message != null) View.VISIBLE else View.GONE
        if (message != null) mBinder.tvMessage.text = message
        if (positiveButtonText != null) mBinder.tvButtonPositive.text = positiveButtonText
        if (negativeButtonText != null) mBinder.tvButtonNegative.text = negativeButtonText
        mBinder.tvButtonPositive.visibility =
            if (onPositiveButtonClick != null) View.VISIBLE else View.GONE
        mBinder.tvButtonNegative.visibility =
            if (onNegativeButtonClick != null) View.VISIBLE else View.GONE
        mBinder.tvButtonPositive.setOnClickListener(this)
        mBinder.tvButtonNegative.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvButtonPositive -> onPositiveButtonClick?.onClick(
                this@MessageDialog,
                0
            )

            R.id.tvButtonNegative -> onNegativeButtonClick?.onClick(
                this@MessageDialog,
                0
            )
        }
    }
}