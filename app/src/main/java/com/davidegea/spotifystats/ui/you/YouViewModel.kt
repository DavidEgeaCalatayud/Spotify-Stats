package com.davidegea.spotifystats.ui.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.usecase.ManageLocalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataControlState(val busy: Boolean = false, val message: String? = null)

@HiltViewModel
class YouViewModel @Inject constructor(private val manage: ManageLocalDataUseCase) : ViewModel() {
    private val mutable = MutableStateFlow(DataControlState())
    val state = mutable.asStateFlow()
    fun export(uri: String) = run("Backup saved. Store it privately; it contains your listening history.") { manage.export(uri) }
    fun restore(uri: String) = run("Backup restored and search rebuilt.") { manage.restore(uri) }
    fun delete() = run("Listening history deleted from this device. Copies you exported remain where you saved them.") { manage.delete() }
    private fun run(success: String, block: suspend () -> Unit) {
        if (mutable.value.busy) return
        mutable.value = DataControlState(busy = true)
        viewModelScope.launch {
            try { block(); mutable.value = DataControlState(message = success) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutable.value = DataControlState(message = "Operation failed. Check the selected file and available storage, then try again.") }
        }
    }
}
