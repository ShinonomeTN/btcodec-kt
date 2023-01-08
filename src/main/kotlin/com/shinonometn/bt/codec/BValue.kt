package com.shinonometn.bt.codec

import java.io.OutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

interface BValue {
    val value : Any
    val typeLiteral : String

    fun encodeTo(output: OutputStream, charset: Charset = StandardCharsets.UTF_8)

    abstract class BBytes: BValue {
        abstract override val value: ByteArray
        override val typeLiteral: String = "byte"

        fun asString(charset: Charset = StandardCharsets.UTF_8) = String(value, charset)

        override fun toString(): String = "$typeLiteral[${value.size}]"
    }

    abstract class BInteger : BValue {
        abstract override val value: BigInteger
        override val typeLiteral: String = "integer"

        fun asByte() : Byte = value.toByte()
        fun asShort() : Short = value.toShort()
        fun asInteger() : Int = value.toInt()
        fun asLong() : Long = value.toLong()

        override fun toString(): String = "$value"
    }

    abstract class BList : BValue {
        abstract override val value: List<BValue>
        override val typeLiteral: String = "list"

        override fun toString(): String = "list { ${value.joinToString(", ") { it.toString() }} }"
    }

    abstract class BDictionary : BValue {
        abstract override val value: Map<String, BValue>
        override val typeLiteral: String = "dictionary"
        override fun toString(): String = "dict { ${value.entries.joinToString("; ") { "${it.key}: ${it.value}" }} }"
    }

    companion object {
        fun wrap(bytes: ByteArray) : BBytes = object : BBytes() {
            override val value = bytes
            override fun encodeTo(output: OutputStream, charset: Charset) {
                output.write("${value.size}:".toByteArray(charset))
                output.write(bytes)
            }
        }

        fun wrap(string: String, charset: Charset = StandardCharsets.UTF_8) : BBytes = wrap(string.toByteArray(charset))

        fun wrap(integer : BigInteger) : BInteger = object : BInteger() {
            override val value: BigInteger = integer
            override fun encodeTo(output: OutputStream, charset: Charset) {
                output.write("i${integer}e".toByteArray(charset))
            }
        }

        fun wrap(int : Int) : BInteger = wrap(int.toBigInteger())

        fun wrap(list : List<BValue>) : BList = object : BList() {
            override val value: List<BValue> = list

            override fun encodeTo(output: OutputStream, charset: Charset) {
                output.write("l".toByteArray(charset))
                value.forEach { it.encodeTo(output, charset) }
                output.write("e".toByteArray(charset))
            }
        }

        fun wrap(map : Map<String, BValue>) : BDictionary = object : BDictionary() {
            override val value: Map<String, BValue> = map
            override fun encodeTo(output: OutputStream, charset: Charset) {
                val keySorted = map.keys.sorted()
                output.write("d".toByteArray(charset))
                keySorted.forEach {
                    val value = map[it]!!
                    val keyBytes = it.toByteArray(charset)
                    output.write("${keyBytes.size}:".toByteArray(charset))
                    output.write(keyBytes)
                    value.encodeTo(output, charset)
                }
                output.write("e".toByteArray(charset))
            }
        }
    }
}