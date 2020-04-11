package net.bjoernpetersen.shutdown

import java.nio.charset.Charset
import java.util.Base64

fun String.encodeBase64(charset: Charset = Charsets.UTF_8): String {
    return Base64.getEncoder().encode(this.toByteArray(charset)).toString(Charsets.US_ASCII)
}

fun String.decodeBase64(charset: Charset = Charsets.UTF_8): String {
    return Base64.getDecoder().decode(this.toByteArray(Charsets.US_ASCII)).toString(charset)
}
