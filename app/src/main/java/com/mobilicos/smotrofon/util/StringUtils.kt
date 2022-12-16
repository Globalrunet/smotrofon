package com.mobilicos.smotrofon.util

class StringUtils {
    companion object {
        fun trimTitle(str: String, length: Int = 50) : String {
            return if (str.length < length) {
                str
            } else {
                var newStr = ""
                for (el in str.split(" ").toTypedArray()) {
                    if (newStr.length + el.length < length) {
                        newStr = "$newStr $el"
                    } else {
                        newStr = "$newStr..."
                        break
                    }
                }
                newStr
            }
        }
    }
}