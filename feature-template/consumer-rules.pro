# ================================
# feature-template模块混淆规则
# ================================

# 保留所有数据模型类
-keep class com.sword.atlas.feature.template.data.model.** { *; }

# 保留所有API接口
-keep interface com.sword.atlas.feature.template.data.api.** { *; }

# 保留所有Repository
-keep class com.sword.atlas.feature.template.data.repository.** { *; }

# 保留所有ViewModel（Hilt会处理，但额外保留确保安全）
-keep class com.sword.atlas.feature.template.ui.**.ViewModel { *; }

# 保留所有Activity和Fragment
-keep class com.sword.atlas.feature.template.ui.**.Activity { *; }
-keep class com.sword.atlas.feature.template.ui.**.Fragment { *; }

# 保留所有Adapter
-keep class com.sword.atlas.feature.template.ui.**.Adapter { *; }
