package com.shinonometn.bt.codec

import net.catten.pt.hamster.bcodec.InvalidBEncodingException
import java.io.EOFException
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class BDecoder(val input : InputStream, val charset : Charset = StandardCharsets.UTF_8) {

    private var indicator = 0
    private fun nextIndicator() : Int {
        if (indicator == 0) indicator = input.read()
        return indicator
    }

    fun nextValue() : BValue? {
        if (nextIndicator() == -1) return null

        return when (indicator) {
            in ('0'.code .. '9'.code) -> decodeBytes()
            'i'.code -> decodeInteger()
            'l'.code -> decodeList()
            'd'.code -> decodeDictionary()
            else -> throw InvalidBEncodingException("Unknown indicator '${indicator.toChar()}'")
        }

    }

    private fun decodeBytes() : BValue.BBytes {
        var byte = nextIndicator().takeIf { (it - '0'.code) in (0 .. 9) }
            ?: throw InvalidBEncodingException("Expected Number but got '${indicator.toChar()}'")

        indicator = 0

        byte -= '0'.code
        var length = 0
        while(byte in (0 .. 9)) {
            length = length * 10 + byte
            byte = readOrError() - '0'.code
        }

        if (byte != (':'.code - '0'.code)) throw InvalidBEncodingException("Colon expected but got '${byte.toChar()}'")
        return BValue.wrap(readBytes(length))
    }

    private fun decodeInteger() : BValue.BInteger {
        nextIndicator().takeIf { it == 'i'.code }
            ?: throw InvalidBEncodingException("Expected 'i' for integer but got '${indicator.toChar()}'")
        indicator = 0

        var counter = 1
        var byte = readOrError()
        if (byte == '0'.code) {
            byte = readOrError()
            if (byte == 'e'.code) return BValue.wrap(BigInteger.ZERO)
            throw InvalidBEncodingException("Illegal leading zero for integer")
        }

        val sb = StringBuilder()
        if (byte == '-'.code) {
            sb.append(byte.toChar())
            byte = readOrError().takeIf { it != '0'.code } ?: throw InvalidBEncodingException("Negative zero is illegal")
        }

        if (byte !in ('0'.code .. '9'.code)) throw InvalidBEncodingException("Digits required but got '${byte.toChar()}'")

        while(byte in ('0'.code .. '9'.code) && counter <= 256) {
            sb.append(byte.toChar())
            counter++
            byte = readOrError()
        }

        if (byte != 'e'.code) throw InvalidBEncodingException("'e' required for ending integer but got '${byte.toChar()}'")
        return BValue.wrap(sb.toString().toBigInteger())
    }

    private fun decodeList() : BValue.BList {
        nextIndicator().takeIf { it == 'l'.code }
            ?: throw InvalidBEncodingException("Expected 'l' but got '${indicator.toChar()}'")
        indicator = 0

        val list = LinkedList<BValue>()
        var newIndicator = nextIndicator()
        while(newIndicator != 'e'.code) {
            list.add(nextValue() ?: break)
            newIndicator = nextIndicator()
        }
        indicator = 0

        return BValue.wrap(list)
    }

    private fun decodeDictionary() : BValue.BDictionary {
        nextIndicator().takeIf { it == 'd'.code }
            ?: throw InvalidBEncodingException("Expected 'd' but got '${indicator.toChar()}'")
        indicator = 0

        val result = mutableMapOf<String, BValue>()
        var newIndicator = nextIndicator()
        while(newIndicator != 'e'.code) {
            val key = nextValue() ?: throw InvalidBEncodingException("Unexpected end of dictionary")
            if (key !is BValue.BBytes) throw InvalidBEncodingException("BByte expected but got ${key::class.simpleName}")
            result[key.asString(charset)] = nextValue() ?: throw InvalidBEncodingException("Unexpected key has no matching value")
            newIndicator = nextIndicator()
        }
        indicator = 0

        return BValue.wrap(result)
    }

    private fun readOrError() = input.read().takeIf { it > 0 } ?: throw EOFException("Unexpected stream ended.")
    private fun readBytes(count : Int) : ByteArray {
        val buffer = ByteArray(count)
        input.read(buffer)
        return buffer
    }
}