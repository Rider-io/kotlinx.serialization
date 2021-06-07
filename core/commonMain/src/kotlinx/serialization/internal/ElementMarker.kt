package kotlinx.serialization.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder

@InternalSerializationApi
@OptIn(ExperimentalSerializationApi::class)
public abstract class ElementMarker(private val descriptor: SerialDescriptor) {
    /*
    Element decoding marks from given bytes.
    The element number is the same as the bit position.
    Marks for the lowest 64 elements are always stored in a single Long value, higher elements stores in long array.
     */
    private var lowerMarks: Long
    private val highMarksArray: LongArray?

    init {
        val elementsCount = descriptor.elementsCount
        if (elementsCount <= Long.SIZE_BITS) {
            lowerMarks = if (elementsCount == Long.SIZE_BITS) {
                // number of bits in the mark is equal to the number of fields
                0L
            } else {
                // (1 - elementsCount) bits are always 1 since there are no fields for them
                -1L shl elementsCount
            }
            highMarksArray = null
        } else {
            lowerMarks = 0L
            // (elementsCount - 1) because only one Long value is needed to store 64 fields etc
            val slotsCount = (elementsCount - 1) / Long.SIZE_BITS
            val elementsInLastSlot = elementsCount % Long.SIZE_BITS
            val highMarks = LongArray(slotsCount)
            // (elementsCount % Long.SIZE_BITS) == 0 this means that the fields occupy all bits in mark
            if (elementsInLastSlot != 0) {
                // all marks except the higher are always 0
                highMarks[highMarks.lastIndex] = -1L shl elementsCount
            }
            highMarksArray = highMarks
        }
    }

    protected abstract fun isPoppedElement(descriptor: SerialDescriptor, index: Int): Boolean

    public fun popUnmarkedIndex(): Int {
        val elementsCount = descriptor.elementsCount
        while (lowerMarks != -1L) {
            val index = lowerMarks.inv().countTrailingZeroBits()
            lowerMarks = lowerMarks or (1L shl index)

            if (isPoppedElement(descriptor, index)) {
                return index
            }
        }

        if (elementsCount > Long.SIZE_BITS) {
            val higherMarks = highMarksArray!!

            for (slot in higherMarks.indices) {
                // (slot + 1) because first element in high marks has index 64
                val slotOffset = (slot + 1) * Long.SIZE_BITS
                // store in a variable so as not to frequently use the array
                var mark = higherMarks[slot]

                while (mark != -1L) {
                    val indexInSlot = mark.inv().countTrailingZeroBits()
                    mark = mark or (1L shl indexInSlot)

                    val index = slotOffset + indexInSlot
                    if (isPoppedElement(descriptor, index)) {
                        higherMarks[slot] = mark
                        return index
                    }
                }
                higherMarks[slot] = mark
            }
            return CompositeDecoder.DECODE_DONE
        }
        return CompositeDecoder.DECODE_DONE
    }

    public fun mark(index: Int) {
        if (index < Long.SIZE_BITS) {
            lowerMarks = lowerMarks or (1L shl index)
        } else {
            val slot = (index / Long.SIZE_BITS) - 1
            val offsetInSlot = index % Long.SIZE_BITS
            highMarksArray!![slot] = highMarksArray[slot] or (1L shl offsetInSlot)
        }
    }
}
