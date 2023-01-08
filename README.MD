# BTCodec KT

Bittorrent protocol encoder/decoder, in Kotlin

## Usage

### Read bt protocol content

```kotlin
val decoder = BDecoder(FileInputStream("file.torrent"))
val value = decoder.nextValue()
println(value)
```

