package kotlinx.serialization.test

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test

class TestClass {
    @Serializable
    data class Nullable(val i: Int?)

    @Serializable
    data class NullableList(val i: List<Int?>)

    @Serializable
    data class NullableMap(val i: Map<Int?, Int?>)

    @Test
    fun foo() {
        println(Json {omitNull = true}.encodeToString(Nullable(null)))
        println(Json {omitNull = true}.encodeToString(NullableList(listOf(null))))
        println(Json {omitNull = true}.encodeToString(NullableMap(mapOf(null to null))))

        println(Json {omitNull = true}.encodeToJsonElement(Nullable(null)))
        println(Json {omitNull = true}.encodeToJsonElement(NullableList(listOf(null))))
        println(Json {omitNull = true}.encodeToJsonElement(NullableMap(mapOf(null to null))))
    }
}
