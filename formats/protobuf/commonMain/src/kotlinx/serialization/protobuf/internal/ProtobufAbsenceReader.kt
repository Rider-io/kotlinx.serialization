package kotlinx.serialization.protobuf.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.internal.ElementMarker

@InternalSerializationApi
@OptIn(ExperimentalSerializationApi::class)
internal class ProtobufAbsenceReader(descriptor: SerialDescriptor) : ElementMarker(descriptor) {
    private var nullValue: Boolean = false

    override fun isPoppedElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (!descriptor.isElementOptional(index)) {
            val elementDescriptor = descriptor.getElementDescriptor(index)
            val kind = elementDescriptor.kind
            if (kind == StructureKind.MAP || kind == StructureKind.LIST) {
                nullValue = false
                return true
            } else if (elementDescriptor.isNullable) {
                nullValue = true
                return true
            }
        }
        return false
    }

    fun popNullValue(): Boolean {
        val prev = nullValue
        nullValue = false
        return prev
    }
}
