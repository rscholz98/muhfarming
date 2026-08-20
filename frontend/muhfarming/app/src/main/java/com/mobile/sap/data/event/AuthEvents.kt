package com.mobile.sap.data.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A tiny process-wide event bus for authentication state changes that originate
 * below the UI layer. The network stack cannot navigate, so when the backend
 * rejects a request as unauthenticated (HTTP 401) the auth interceptor in
 * [com.mobile.sap.data.api.RetrofitClient] clears the session and emits
 * [unauthorized]. The navigation host subscribes and drives a forced logout back
 * to the login screen.
 *
 * Mirrors [DataEvents]: a replay of 0 (only live subscribers are notified) with a
 * small buffer so emits from OkHttp's non-suspending interceptor thread never drop.
 */
object AuthEvents {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /** Emitted when the backend rejects the stored token (HTTP 401). */
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    /** Signal that the current session is no longer valid. Non-suspending. */
    fun emitUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
