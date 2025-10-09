# ================================
# core-ui模块混淆规则
# ================================

# ================================
# ViewBinding混淆规则
# ================================

# 保留ViewBinding类
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
    public static *** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# 保留ViewBinding扩展函数
-keep class com.sword.atlas.core.ui.ext.ViewBindingExt** { *; }

# ================================
# 自定义View混淆规则
# ================================

# 保留自定义View的构造方法
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留自定义控件
-keep class com.sword.atlas.core.ui.widget.** { *; }

# 保留自定义View的属性方法
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# ================================
# Activity和Fragment基类
# ================================

# 保留Activity基类
-keep class com.sword.atlas.core.ui.base.BaseActivity { *; }
-keep class com.sword.atlas.core.ui.base.BaseVMActivity { *; }

# 保留Fragment基类
-keep class com.sword.atlas.core.ui.base.BaseFragment { *; }
-keep class com.sword.atlas.core.ui.base.BaseVMFragment { *; }

# ================================
# Adapter和ViewHolder
# ================================

# 保留Adapter基类
-keep class com.sword.atlas.core.ui.adapter.BaseAdapter { *; }
-keep class com.sword.atlas.core.ui.adapter.BaseViewHolder { *; }
-keep class com.sword.atlas.core.ui.adapter.MultiTypeAdapter { *; }

# ================================
# Dialog
# ================================

# 保留Dialog类
-keep class com.sword.atlas.core.ui.dialog.** { *; }

# ================================
# 其他UI组件
# ================================

# 保留StateLayout
-keep class com.sword.atlas.core.ui.widget.StateLayout { *; }

# 保留TitleBar
-keep class com.sword.atlas.core.ui.widget.TitleBar { *; }

# 保留LoadingButton
-keep class com.sword.atlas.core.ui.widget.LoadingButton { *; }
