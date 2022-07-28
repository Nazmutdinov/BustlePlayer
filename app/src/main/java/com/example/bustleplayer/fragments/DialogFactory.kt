package com.example.bustleplayer.fragments

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.bustleplayer.R
import com.google.android.material.textfield.TextInputEditText


class DialogFactory {
    /**
     * диалог для ввода названия нового ключевого слова
     */
    fun showAddPlaylistDialog(context: Context, callbackAddClick: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_add_playlist, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.title_edit_text)

        builder.setTitle(context.getString(R.string.add_keyword_dialog_title))
            .setMessage(context.getString(R.string.add_keyword_dialog_message))
            .setView(dialogView)
            .setNegativeButton(R.string.dialog_button_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton(R.string.dialog_button_add) { dialogInterface, _ ->
                val ts = editText.text.toString()
                callbackAddClick(ts)

                dialogInterface.dismiss()
            }
            .create()
            .show()
    }
}