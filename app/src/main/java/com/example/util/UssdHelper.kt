package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UssdHelper {

    fun dialUssd(context: Context, code: String) {
        try {
            val encodedCode = Uri.encode(code)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encodedCode")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, code: String, label: String = "USSD Code") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, code)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied $code to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy $code", Toast.LENGTH_SHORT).show()
        }
    }
}
