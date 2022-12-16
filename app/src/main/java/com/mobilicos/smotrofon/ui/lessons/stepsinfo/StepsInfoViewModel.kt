package com.mobilicos.smotrofon.ui.lessons.stepsinfo

import androidx.lifecycle.ViewModel
import com.mobilicos.smotrofon.data.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StepsInfoViewModel @Inject constructor() : ViewModel() {
    var item: Item? = null
    var currentStep: Int = 0
    var currentFrame: Int = 0
    var stepsCount: Int = 0
    var framesCount: Int = 0
    var animationInterval: Long = 500

    fun getStepDescription(): String {
        return item?.steps?.get(currentStep)?.text ?:""
    }

    fun stepsCount(): Int {
        return item?.steps?.size ?: 0
    }

    fun framesCount(): Int {
        return item?.steps?.get(currentStep)?.frames?.size ?: 0
    }

    fun increaseStep() {
        if (currentStep + 1 < item?.steps?.size ?: 0) {
            currentStep ++
        } else {
            currentStep = 0
        }
    }

    fun decreaseStep() {
        if (currentStep > 0) {
            currentStep --
        }
    }

    fun increaseFrame() {
        currentFrame++
    }

    fun hasCurrentFrame(): Boolean {
        return currentFrame < item?.steps?.get(currentStep)?.frames?.size ?: 0
    }

    fun hasNextStep(): Boolean {
        return currentStep < (item?.steps?.size ?: 0) - 1
    }

    fun getCurrentFrameFileName(): String {
        return "i_${item?.ident}_${item?.steps?.get(currentStep)?.stepNumber}_${item?.steps?.get(currentStep)?.frames?.get(currentFrame)?.sortOrder}${item?.iconExtension}"
    }
}