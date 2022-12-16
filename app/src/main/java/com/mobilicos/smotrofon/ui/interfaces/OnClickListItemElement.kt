package com.mobilicos.smotrofon.ui.interfaces

interface OnClickListItemElement<T> {
    fun clickOnListItemElement(element: T, type: Int = 0, position: Int = -1)
}