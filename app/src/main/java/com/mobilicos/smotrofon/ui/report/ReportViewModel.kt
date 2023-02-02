package com.mobilicos.smotrofon.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.data.queries.ReportAddQuery
import com.mobilicos.smotrofon.data.repositories.ReportRepository
import com.mobilicos.smotrofon.data.responses.ReportAddResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(private val reportRepository: ReportRepository) : ViewModel() {

    var appLabel: String = ""
    var model: String = ""
    var objectId: Int = 0
    var key: String = ""
    var currentReportReasonPosition: Int = -1
    var reportViewId: Int = -1

    private val _reportResult = MutableStateFlow<Result<ReportAddResponse>>(Result.ready())
    val reportResult: StateFlow<Result<ReportAddResponse>> = _reportResult

    private val _reportAddResult = MutableStateFlow<Boolean?>(null)
    val reportAddResult: StateFlow<Boolean?> = _reportAddResult

    fun addReport(text: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val query = ReportAddQuery(app_label = appLabel,
                model = model,
                object_id = objectId,
                report_reason = currentReportReasonPosition,
                text = text,
                key = key)
            reportRepository.addReportData(q = query).collect {
                println("RESULT ADD REPORT DATA: $it")
                _reportResult.value = it
            }
        }
    }

    fun setReportAddResult(value: Boolean?) {
        _reportAddResult.value = value
    }

    fun clearReportResult() {
        _reportResult.value = Result.ready()
    }
}