package com.example.pclink.touchpad;

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import com.example.pclink.NetworkLink

public class Keyboard(val net: NetworkLink, val view: View, val keyboardButton: AppCompatImageButton, val hiddenInput: EditText, val context: Context) {

    var inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    public fun setListener(){
        keyboardButton.setOnClickListener {
            openKeyboard()
        }

//        hiddenInput.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val newText = s?.toString() ?: ""
//                if (newText.isNotEmpty()) {
//                    val typedChar = newText.last()
//                    sendKeyToPc(typedChar)
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                // очищаем поле чтобы каждый символ воспринимался отдельно
//                hiddenInput.text.clear()
//            }
//        })
        hiddenInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> { // Backspace
                        sendSpecialKeyToPc("BACKSPACE")
                        return@setOnKeyListener true
                    }

                    0x0D -> { // Enter
                        sendSpecialKeyToPc("ENTER")
                        return@setOnKeyListener true
                    }

                    KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                        sendSpecialKeyToPc("LANG_SWITCH")
                        return@setOnKeyListener true
                    }

                    // Дополнительно можно обработать: Shift, Tab, Ctrl и др.
                }
            }
            false
        }

        hiddenInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    val inputChar = s?.get(start)
//                    if (inputChar == '\n')
//                        sendSpecialKeyToPc("ENTER")
//                    if (inputChar == ' ')
//                        sendSpecialKeyToPc("SPACE")
                    if (inputChar != null) {
                        sendCharToPc(inputChar)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                s?.clear()
            }
        })


    }
    public fun openKeyboard() {
        hiddenInput.visibility = View.VISIBLE
        hiddenInput.requestFocus()
        inputManager.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun sendKeyCodeToPc(androidKeyCode: Int) {
        val atwkm = AndroidToWindowsKeyMapping()
        val windowsKeyCode = atwkm.getCodes()[androidKeyCode]
        if (windowsKeyCode != null) {
            net.sendCommand(5, "KEYPRESS", arrayOf(windowsKeyCode.toString()))
            Log.d("KeySend", "Отправлен Windows keycode: $windowsKeyCode")
        }
    }
    private fun sendCharToPc(key: Char) {
        // Здесь реализуйте отправку символа по сети
        // Например: net.send("KEY:$char")
        Log.d("PRESSED", key.toString())
        val atwkm = AndroidToWindowsKeyMapping()
        val windowsKeyCode = atwkm.charToScanCode(key)
        if(key.isLetterOrDigit()){
            if (key.isLowerCase() || key.isDigit()){
                Toast.makeText(context, "Символ: $windowsKeyCode", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(windowsKeyCode.toString(), false.toString()))
            } else {
                Toast.makeText(context, "Символ c shift: $windowsKeyCode", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(windowsKeyCode.toString(), true.toString()))
            }

        }else{
            if(key == '(' || key == ')' || key == '@' || key == '#' || key == '_' || key == '&' || key == '+' || key == '*' || key == ':' ||
                key == '!' || key == '?' || key == '$' || key == '~' || key == '\'' || key == '{' || key == '}' || key == '%'){
                Toast.makeText(context, "Символ: $windowsKeyCode", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(windowsKeyCode.toString(), true.toString()))
            } else {
                Toast.makeText(context, "Символ: $windowsKeyCode", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(windowsKeyCode.toString(), false.toString()))
            }
        }
    }
    private fun sendSpecialKeyToPc(type: String) {
        when (type) {
            "BACKSPACE" -> net.sendCommand(5, "SPECIAL_KEY", arrayOf("BACKSPACE"))
            "ENTER" -> net.sendCommand(5, "SPECIAL_KEY", arrayOf("ENTER"))
            "SPACE" -> net.sendCommand(5, "SPECIAL_KEY", arrayOf("SPACE"))
            "LANG_SWITCH" -> net.sendCommand(5, "SPECIAL_KEY", arrayOf("LANG_SWITCH"))
            // Здесь можно добавить SHIFT, TAB и другие
        }
    }

    private fun sendKeyToPc(key: Char) {
        // Здесь реализуйте отправку символа по сети
        // Например: net.send("KEY:$char")
        Log.d("PRESSED", key.toString())
        if(key.isLetterOrDigit()){
            if (key.isLowerCase() || key.isDigit()){
                Toast.makeText(context, "Символ: $key", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(key.toString(), false.toString()))
            } else {
                Toast.makeText(context, "Символ c shift: $key", Toast.LENGTH_SHORT).show()
                net.sendCommand(5, "KEYPRESS", arrayOf(key.toString(), true.toString()))
            }

        } else if (key.isDigit()){

        }
    }
}
