package app.forku.core.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Utility functions for safe Flow error handling
 * Prevents Flow exception transparency violations
 */

/**
 * Safely emit a failure result, avoiding AbortFlowException
 * @param exception The exception to handle
 * @param emitFunction Function to emit the failure result
 */
inline fun <T> safeEmitFailure(
    exception: Exception,
    emitFunction: (Result<T>) -> Unit
) {
    // Don't emit AbortFlowException to avoid Flow transparency violation
    // We can't directly check for AbortFlowException since it's internal,
    // but we can check for the specific message pattern
    val isAbortFlow = exception.message?.contains("Flow was aborted") == true ||
                     exception.javaClass.simpleName == "AbortFlowException"
    
    if (!isAbortFlow) {
        emitFunction(Result.failure(exception))
    }
}

/**
 * Extension function to create a safe Flow with error handling
 * @param block The flow block to execute
 * @param onError Function to handle errors (optional)
 */
inline fun <T> safeFlow(
    crossinline block: suspend () -> T,
    crossinline onError: (Exception) -> Unit = {}
): Flow<Result<T>> = flow {
    try {
        val result = block()
        emit(Result.success(result))
    } catch (e: Exception) {
        onError(e)
        safeEmitFailure(e) { failure ->
            emit(failure)
        }
    }
}

/**
 * Extension function to add safe error handling to existing flows
 */
inline fun <T> Flow<Result<T>>.safeCatch(
    crossinline onError: (Throwable) -> Unit = {}
): Flow<Result<T>> = this.catch { e ->
    onError(e)
    if (e is Exception) {
        safeEmitFailure(e) { failure ->
            emit(failure)
        }
    }
} 