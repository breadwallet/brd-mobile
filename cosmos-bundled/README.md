# Cosmos Bundled

This module depends on all of Cosmos' feature modules to produce Jvm jars and iOS compatibly `.frameworks`.
These outputs allow consumes to depend on all of Cosmos' features without custom build logic or knowing about all modules.

```shell
# Package iOS Frameworks (Simulator)
./gradlew linkDebugFrameworkIosX64 linkReleaseFrameworkIosX64
# Package iOS Frameworks (Device)
./gradlew linkDebugFrameworkIosArm64 linkReleaseFrameworkIosArm64
```
