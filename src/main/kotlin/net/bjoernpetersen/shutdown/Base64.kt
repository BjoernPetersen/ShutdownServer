package net.bjoernpetersen.shutdown

import java.util.*

fun String.encodeBase64(): String {
    return Base64.getEncoder().encode(this.toByteArray()).toString(Charsets.UTF_8)
}

fun String.decodeBase64(): String {
    return Base64.getDecoder().decode(this.toByteArray()).toString(Charsets.UTF_8)
}
