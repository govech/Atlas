# Router specific consumer rules
# Keep all router classes and interfaces
-keep class com.sword.atlas.core.router.** { *; }

# Keep annotation classes
-keep @interface com.sword.atlas.core.router.annotation.** { *; }

# Keep classes with Route annotation
-keep @com.sword.atlas.core.router.annotation.Route class * { *; }

# Keep RouteInterceptor implementations
-keep class * implements com.sword.atlas.core.router.interceptor.RouteInterceptor { *; }

# Keep callback interfaces
-keep interface com.sword.atlas.core.router.callback.** { *; }