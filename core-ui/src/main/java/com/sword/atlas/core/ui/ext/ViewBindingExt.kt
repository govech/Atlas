package com.sword.atlas.core.ui.ext

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Activity ViewBinding扩展函数
 *
 * 简化ViewBinding的初始化代码
 * 使用lazy委托延迟初始化
 *
 * 使用示例：
 * ```
 * class MainActivity : AppCompatActivity() {
 *     private val binding by viewBinding<ActivityMainBinding>()
 * }
 * ```
 *
 * @param VB ViewBinding类型
 * @return ViewBinding实例的Lazy委托
 */
inline fun <reified VB : ViewBinding> AppCompatActivity.viewBinding(): Lazy<VB> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val inflateMethod = VB::class.java.getMethod(
            "inflate",
            LayoutInflater::class.java
        )
        @Suppress("UNCHECKED_CAST")
        inflateMethod.invoke(null, layoutInflater) as VB
    }
}

/**
 * Fragment ViewBinding扩展函数
 *
 * 简化ViewBinding的初始化代码
 * 使用lazy委托延迟初始化
 *
 * 注意：Fragment中应该在onDestroyView中将binding设置为null
 *
 * 使用示例：
 * ```
 * class HomeFragment : Fragment() {
 *     private var _binding: FragmentHomeBinding? = null
 *     private val binding get() = _binding!!
 *
 *     override fun onCreateView(...): View {
 *         _binding = viewBinding()
 *         return binding.root
 *     }
 *
 *     override fun onDestroyView() {
 *         super.onDestroyView()
 *         _binding = null
 *     }
 * }
 * ```
 *
 * @param VB ViewBinding类型
 * @param inflater LayoutInflater
 * @param container ViewGroup容器
 * @return ViewBinding实例
 */
inline fun <reified VB : ViewBinding> Fragment.viewBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
): VB {
    val inflateMethod = VB::class.java.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
    @Suppress("UNCHECKED_CAST")
    return inflateMethod.invoke(null, inflater, container, false) as VB
}
