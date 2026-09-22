package com.davidegea.spotifystats.ui.you

import com.davidegea.spotifystats.R
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.usecase.ManageLocalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataControlState(val busy: Boolean = false, @StringRes val messageRes: Int? = null)

@HiltViewModel
class YouViewModel @Inject constructor(private val manage: ManageLocalDataUseCase) : ViewModel() {
    private val mutable = MutableStateFlow(DataControlState())
    val state = mutable.asStateFlow()
    fun export(uri: String) = run(R.string.you_export_success) { manage.export(uri) }
    fun restore(uri: String) = run(R.string.you_restore_success) { manage.restore(uri) }
    fun delete() = run(R.string.you_delete_success) { manage.delete() }
    private fun run(@StringRes success: Int, block: suspend () -> Unit) {
        if (mutable.value.busy) return
        mutable.value = DataControlState(busy = true)
        viewModelScope.launch {
            try { block(); mutable.value = DataControlState(messageRes = success) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutable.value = DataControlState(messageRes = R.string.you_operation_error) }
        }
    }
}
