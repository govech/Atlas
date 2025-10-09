package com.sword.atlas.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 协程调度器提供者接口
 *
 * 用于提供不同的协程调度器，便于测试时Mock
 */
interface DispatcherProvider {
    /**
     * 主线程调度器
     *
     * 用于UI操作
     */
    val main: CoroutineDispatcher
    
    /**
     * IO线程调度器
     *
     * 用于网络请求、文件读写等IO操作
     */
    val io: CoroutineDispatcher
    
    /**
     * Default线程调度器
     *
     * 用于CPU密集型操作
     */
    val default: CoroutineDispatcher
}

/**
 * 默认协程调度器提供者
 *
 * 提供标准的协程调度器实现
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
