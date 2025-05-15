package com.example.pclink.touchpad

import android.view.KeyEvent

class AndroidToWindowsKeyMapping {
    public fun getCodes(): Map<Int, Int>{
        return AndroidToWindowsKeyCodeMap
    }
    fun charToScanCode(char: Char): Int? {
        return when (char) {
            in '0'..'9' -> char.code                     // '0' -> 0x30, ..., '9' -> 0x39
            in 'A'..'Z' -> char.code                     // 'A' -> 0x41, ..., 'Z' -> 0x5A
            in 'a'..'z' -> char.uppercaseChar().code     // 'a' -> 'A' -> 0x41, и т.д.

            ' ' -> 0x20
            '\n' -> 0x0D // Enter
            '\b' -> 0x08 // Backspace
            '\t' -> 0x09 // Tab

            '-' -> 0xBD
            '~' -> 0xC0 //?
            '{' -> 0xDB //?
            '}' -> 0xDD //?
            '(' -> 0x39 //9 + SHIFT
            ')' -> 0x30 //0 + SHIFT
            '<' -> 0xE3 //
            '>' -> 0xE4 //
            '@' -> 0x32 //2 + SHIFT
            '#' -> 0x33 //3 + SHIFT
            '$' -> 0x34 //4 + SHIFT
            '_' -> 0xBD //- + SHIFT
            '&' -> 0x37 //7 + SHIFT
            '+' -> 0xBB //= + SHIFT
            '*' -> 0x38 //8 + SHIFT
            '\"' -> 0xDE //
            ':' -> 0xBA //; + SHIFT
            '!' -> 0x31 //1 + SHIFT
            '?' -> 0xBF //? + SHIFT
            '%' -> 0x35 //? + SHIFT
            '=' -> 0xBB
            '[' -> 0xDB
            ']' -> 0xDD
            '\\' -> 0xDC
            ';' -> 0xBA
            '\'' -> 0xDE
            ',' -> 0xBC
            '.' -> 0xBE
            '/' -> 0xBF
            '`' -> 0xC0

            else -> null
        }
    }


    val AndroidToWindowsKeyCodeMap = mapOf(
        // Letters A–Z
        KeyEvent.KEYCODE_A to 0x41,
        KeyEvent.KEYCODE_B to 0x42,
        KeyEvent.KEYCODE_C to 0x43,
        KeyEvent.KEYCODE_D to 0x44,
        KeyEvent.KEYCODE_E to 0x45,
        KeyEvent.KEYCODE_F to 0x46,
        KeyEvent.KEYCODE_G to 0x47,
        KeyEvent.KEYCODE_H to 0x48,
        KeyEvent.KEYCODE_I to 0x49,
        KeyEvent.KEYCODE_J to 0x4A,
        KeyEvent.KEYCODE_K to 0x4B,
        KeyEvent.KEYCODE_L to 0x4C,
        KeyEvent.KEYCODE_M to 0x4D,
        KeyEvent.KEYCODE_N to 0x4E,
        KeyEvent.KEYCODE_O to 0x4F,
        KeyEvent.KEYCODE_P to 0x50,
        KeyEvent.KEYCODE_Q to 0x51,
        KeyEvent.KEYCODE_R to 0x52,
        KeyEvent.KEYCODE_S to 0x53,
        KeyEvent.KEYCODE_T to 0x54,
        KeyEvent.KEYCODE_U to 0x55,
        KeyEvent.KEYCODE_V to 0x56,
        KeyEvent.KEYCODE_W to 0x57,
        KeyEvent.KEYCODE_X to 0x58,
        KeyEvent.KEYCODE_Y to 0x59,
        KeyEvent.KEYCODE_Z to 0x5A,

        // Numbers 0–9
        KeyEvent.KEYCODE_0 to 0x30,
        KeyEvent.KEYCODE_1 to 0x31,
        KeyEvent.KEYCODE_2 to 0x32,
        KeyEvent.KEYCODE_3 to 0x33,
        KeyEvent.KEYCODE_4 to 0x34,
        KeyEvent.KEYCODE_5 to 0x35,
        KeyEvent.KEYCODE_6 to 0x36,
        KeyEvent.KEYCODE_7 to 0x37,
        KeyEvent.KEYCODE_8 to 0x38,
        KeyEvent.KEYCODE_9 to 0x39,

        // Function keys
        KeyEvent.KEYCODE_F1 to 0x70,
        KeyEvent.KEYCODE_F2 to 0x71,
        KeyEvent.KEYCODE_F3 to 0x72,
        KeyEvent.KEYCODE_F4 to 0x73,
        KeyEvent.KEYCODE_F5 to 0x74,
        KeyEvent.KEYCODE_F6 to 0x75,
        KeyEvent.KEYCODE_F7 to 0x76,
        KeyEvent.KEYCODE_F8 to 0x77,
        KeyEvent.KEYCODE_F9 to 0x78,
        KeyEvent.KEYCODE_F10 to 0x79,
        KeyEvent.KEYCODE_F11 to 0x7A,
        KeyEvent.KEYCODE_F12 to 0x7B,

        // Special keys
        KeyEvent.KEYCODE_ENTER to 0x0D,
        KeyEvent.KEYCODE_DEL to 0x08, // Backspace
        KeyEvent.KEYCODE_TAB to 0x09,
        KeyEvent.KEYCODE_SPACE to 0x20,
        KeyEvent.KEYCODE_ESCAPE to 0x1B,
        KeyEvent.KEYCODE_BACK to 0x1B, // Esc fallback

        // Arrows
        KeyEvent.KEYCODE_DPAD_UP to 0x26,
        KeyEvent.KEYCODE_DPAD_DOWN to 0x28,
        KeyEvent.KEYCODE_DPAD_LEFT to 0x25,
        KeyEvent.KEYCODE_DPAD_RIGHT to 0x27,

        // Shift/Ctrl/Alt
        KeyEvent.KEYCODE_SHIFT_LEFT to 0xA0,
        KeyEvent.KEYCODE_SHIFT_RIGHT to 0xA1,
        KeyEvent.KEYCODE_CTRL_LEFT to 0xA2,
        KeyEvent.KEYCODE_CTRL_RIGHT to 0xA3,
        KeyEvent.KEYCODE_ALT_LEFT to 0xA4,
        KeyEvent.KEYCODE_ALT_RIGHT to 0xA5,

        // Symbols and punctuation
        KeyEvent.KEYCODE_PERIOD to 0xBE,
        KeyEvent.KEYCODE_COMMA to 0xBC,
        KeyEvent.KEYCODE_MINUS to 0xBD,
        KeyEvent.KEYCODE_EQUALS to 0xBB,
        KeyEvent.KEYCODE_SEMICOLON to 0xBA,
        KeyEvent.KEYCODE_APOSTROPHE to 0xDE,
        KeyEvent.KEYCODE_SLASH to 0xBF,
        KeyEvent.KEYCODE_BACKSLASH to 0xDC,
        KeyEvent.KEYCODE_LEFT_BRACKET to 0xDB,
        KeyEvent.KEYCODE_RIGHT_BRACKET to 0xDD,
        KeyEvent.KEYCODE_GRAVE to 0xC0,

        // Other common keys
        KeyEvent.KEYCODE_INSERT to 0x2D,
        KeyEvent.KEYCODE_FORWARD_DEL to 0x2E,
        KeyEvent.KEYCODE_MOVE_HOME to 0x24,
        KeyEvent.KEYCODE_MOVE_END to 0x23,
        KeyEvent.KEYCODE_PAGE_UP to 0x21,
        KeyEvent.KEYCODE_PAGE_DOWN to 0x22,
        KeyEvent.KEYCODE_NUM_LOCK to 0x90,
        KeyEvent.KEYCODE_CAPS_LOCK to 0x14,
        KeyEvent.KEYCODE_SCROLL_LOCK to 0x91,
        KeyEvent.KEYCODE_BREAK to 0x13,
        KeyEvent.KEYCODE_MENU to 0x5D
    )

}