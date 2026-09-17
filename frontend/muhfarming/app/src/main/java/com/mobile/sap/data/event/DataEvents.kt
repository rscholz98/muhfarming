package com.mobile.sap.data.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The kinds of reference data that can change at runtime and that other
 * screens care about. Emitted after a successful create so subscribed
 * ViewModels can refresh without a manual pull.
 */
enum class DataChange {
    Cultivation,
    Hazard,
    Guideline,
    Risk,
    Incident,
    Alert,
    Farm
}

/**
 * A tiny process-wide event bus for cross-screen data freshness. Repositories
 * emit a [DataChange] after a successful write; ViewModels on other tabs
 * subscribe and refresh the relevant lists. This keeps the per-ViewModel
 * repository pattern intact while letting, e.g., a cultivation-risk created in
 * the Guide tab appear immediately in the Alerts incident form.
 *
 * Uses a replay of 0 (only live subscribers are notified) with a small buffer
 * so emits from non-suspending contexts never drop.
 */
object DataEvents {
    private val _events = MutableSharedFlow<DataChange>(extraBufferCapacity = 16)
    val events: SharedFlow<DataChange> = _events.asSharedFlow()

    /** Emit a change. Non-suspending; safe to call from repositories. */
    fun emit(change: DataChange) {
        _events.tryEmit(change)
    }
}
