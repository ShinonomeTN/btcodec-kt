# BTCodec KT

Bittorrent protocol encoder/decoder, in Kotlin.

References:
- [Github: mpetazzoni/ttorrent](https://github.com/mpetazzoni/ttorrent)
- [Bittorrent: bep_0003](http://www.bittorrent.org/beps/bep_0003.html)

## Usage

For repository url, please see [ShinonomeTN Public Maven Repository](https://github.com/ShinonomeTN/maven-public)

Maven: 
```xml
<dependency>
    <groupId>com.shinonometn</groupId>
    <artifactId>btcodec-kt</artifactId>
    <version>1.0</version>
</dependency>
```

Gradle:
```groovy
implementation("com.shinonometn:btcodec-kt:1.0")
```

### Read bt protocol content

```kotlin
val decoder = BDecoder(FileInputStream("file.torrent"))
val value = decoder.nextValue()
println(value)
```

