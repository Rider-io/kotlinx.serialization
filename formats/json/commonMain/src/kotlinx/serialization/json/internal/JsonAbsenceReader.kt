package kotlinx.serialization.json.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.ElementMarker

@InternalSerializationApi
@OptIn(ExperimentalSerializationApi::class)
internal class JsonAbsenceReader(descriptor: SerialDescriptor) : ElementMarker(descriptor) {
    internal var poppedNull: Boolean = false
        private set

    override fun isPoppedElement(descriptor: SerialDescriptor, index: Int): Boolean {
        poppedNull = !descriptor.isElementOptional(index) && descriptor.getElementDescriptor(index).isNullable
        return poppedNull
    }
}
