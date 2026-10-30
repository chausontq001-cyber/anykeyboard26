# Funput Telex engine

Telex composition is provided by the MIT-licensed Funput Rust engine, pinned as
the `third_party/funput` Git submodule. Initialize it after cloning:

```bash
git submodule update --init --recursive
rustup target add aarch64-linux-android x86_64-linux-android
```

Android packaging compiles `funput-jni` for `arm64-v8a` and `x86_64` through
Funput's build script. Install NDK `29.0.14206865`; the Gradle build sets the
SDK path for the native build task. The VNI implementation remains in
`VietnameseEngine`.
