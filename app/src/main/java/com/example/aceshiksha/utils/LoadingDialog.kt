package com.nikhil.aceshiksha.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.nikhil.aceshiksha.R

class LoadingDialog(private val context: Context) {
    private var dialog: Dialog? = null

    fun show() {
        dialog = Dialog(context).apply {
            setContentView(R.layout.dialog_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}