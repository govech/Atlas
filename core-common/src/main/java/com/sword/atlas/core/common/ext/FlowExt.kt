package com.sword.atlas.core.common.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sword.atlas.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Flow扩展函数
 */

/**
 * 在主线程收集Flow
 *
 * @param T 数据类型
 * @param lifecycleOwner 生命周期所有者
 * @param minActiveState 最小活跃状态
 * @param action 收集回调
 */
fun <T> Flow<T>.collectOnMain(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect { action(it) }
        }
    }
}

/**
 * 处理Result类型的Flow
 *
 * @param T 数据类型
 * @param onSuccess 成功回调
 * @param onError 失败回调
 * @return Flow
 */
fun <T> Flow<Result<T>>.onResult(
    onSuccess: (T) -> Unit,
    onError: (Int, String) -> Unit
): Flow<Result<T>> {
    return this.onEach { result ->
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> onError(result.code, result.message)
        }
    }
}

/**
 * 处理Result类型的Flow（简化版）
 *
 * @param T 数据类型
 * @param onSuccess 成功回调
 * @param onError 失败回调（只接收消息）
 * @return Flow
 */
fun <T> Flow<Result<T>>.onResult(
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
): Flow<Result<T>> {
    return this.onEach { result ->
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> onError(result.message)
        }
    }
}

/**
 * 捕获Flow异常
 *
 * @param T 数据类型
 * @param onError 异常回调
 * @return Flow
 */
fun <T> Flow<T>.catchError(
    onError: (Throwable) -> Unit
): Flow<T> {
    return this.catch { throwable ->
        onError(throwable)
    }
}

/**
 * 在生命周期内收集Result类型的Flow
 *
 * @param T 数据类型
 * @param lifecycleOwner 生命周期所有者
 * @param minActiveState 最小活跃状态
 * @param onSuccess 成功回调
 * @param onError 失败回调
 */
fun <T> Flow<Result<T>>.collectResult(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onSuccess: (T) -> Unit,
    onError: (Int, String) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect { result ->
                when (result) {
                    is Result.Success -> onSuccess(result.data)
                    is Result.Error -> onError(result.code, result.message)
                }
            }
        }
    }
}

/**
 * 在生命周期内收集Result类型的Flow（简化版）
 *
 * @param T 数据类型
 * @param lifecycleOwner 生命周期所有者
 * @param minActiveState 最小活跃状态
 * @param onSuccess 成功回调
 * @param onError 失败回调（只接收消息）
 */
fun <T> Flow<Result<T>>.collectResult(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect { result ->
                when (result) {
                    is Result.Success -> onSuccess(result.data)
                    is Result.Error -> onError(result.message)
                }
            }
        }
    }
}
