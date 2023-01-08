package com.shinonometn.bt.codec

import net.catten.pt.hamster.bcodec.InvalidBEncodingException
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class BDecoderTest {

    @Test
    fun `Test read file`() {
        val decoder = BDecoder(this::class.java.getResourceAsStream("/src.torrent")!!)
        val btValue = decoder.nextValue()
        assertNotNull(btValue)
        println(btValue)
    }

    @Test
    fun `Test Good Number`() {
        testNumber(0)
        testNumber(1)
        testNumber(Int.MAX_VALUE.toLong())
        testNumber(1234567)
        testNumber(-1)
        testNumber(-100)
        testNumber(Int.MIN_VALUE.toLong())
        testNumber(Long.MAX_VALUE)
        testNumber(Long.MIN_VALUE)
    }

    @Test
    fun `Test Bad Number`() {
        //by specification number with lead zero it's incorrect value
        testBadNumber("00")
        testBadNumber("01234")
        testBadNumber("000")
        testBadNumber("0001")
    }

    private fun testNumber(value: Long) {
        assertEquals(value, (BDecoder(ByteArrayInputStream(numberToBEPBytes(value))).nextValue() as? BValue.BInteger)?.value?.toLong())
    }

    private fun testBadNumber(number: String) {
        try {
            BDecoder(ByteArrayInputStream(numberToBEPBytes(number))).nextValue()
        } catch (e: InvalidBEncodingException) {
            // Pass
            return
        }
        fail("Value $number is incorrect by BEP specification but is was parsed correctly")
    }

    private fun numberToBEPBytes(value: Long): ByteArray {
        return ("i" + value + "e").toByteArray(charset("ASCII"))
    }

    private fun numberToBEPBytes(value: String): ByteArray {
        return ("i" + value + "e").toByteArray(charset("ASCII"))
    }
}