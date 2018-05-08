package amf.core.remote

/** Migrated code from Rhino */
class RhinoEncoder {

  /*
   *   ECMA 3, 15.1.3 URI Handling Function Properties
   *
   *   The following are implementations of the algorithms
   *   given in the ECMA specification for the hidden functions
   *   'Encode' and 'Decode'.
   */
  def encode(str: String): String = {
    var utf8buf: Array[Byte] = null
    var sb: StringBuilder    = null

    var k = 0
    while (k < str.length) {
      val C = str(k)
      if (encodeUnescaped(C)) {
        if (sb != null) {
          sb.append(C)
        }
      } else {
        if (sb == null) {
          sb = new StringBuilder(str.length + 3)
          sb.append(str)
          sb.setLength(k)

          utf8buf = Array.ofDim[Byte](6)
        }

        if (0xDC00 <= C && C <= 0xDFFF) {
          throw uriError
        }
        var V: Int = 0

        if (C < 0xD800 || 0xDBFF < C) {
          V = C
        } else {
          k = k + 1
          if (k == str.length) {
            throw uriError
          }
          val C2: Char = str(k)

          if (!(0xDC00 <= C2 && C2 <= 0xDFFF)) {
            throw uriError
          }
          V = ((C - 0xD800) << 10) + (C2 - 0xDC00) + 0x10000
        }
        val L: Int = oneUcs4ToUtf8Char(utf8buf, V)
        var j: Int = 0
        while (j < L) {
          val d: Int = 0xff & utf8buf(j)
          sb.append('%')
          sb.append(toHexChar(d >>> 4))
          sb.append(toHexChar(d & 0xf))
          j = j + 1
        }
      }
      k = k + 1
    }
    if (sb == null) str else sb.toString
  }

  private def toHexChar(i: Int) = {
    if ((i >> 4) != 0) throw uriError //Kit.codeBug
    (if (i < 10) i + '0'
     else i - 10 + 'A').toChar
  }

  /* Convert one UCS-4 char and write it into a UTF-8 buffer, which must be
   * at least 6 bytes long.  Return the number of UTF-8 bytes of data written.
   */
  private def oneUcs4ToUtf8Char(utf8Buffer: Array[Byte], ucs: Int) = {
    var utf8Length = 1
    //JS_ASSERT(ucs4Char <= 0x7FFFFFFF);
    var ucs4Char = ucs
    if ((ucs4Char & ~0x7F) == 0) utf8Buffer(0) = ucs4Char.toByte
    else {
      var i = 0
      var a = ucs4Char >>> 11
      utf8Length = 2
      while ({
        a != 0
      }) {
        a >>>= 5
        utf8Length += 1
      }
      i = utf8Length
      while ({
        {
          i -= 1; i
        } > 0
      }) {
        utf8Buffer(i) = ((ucs4Char & 0x3F) | 0x80).toByte
        ucs4Char >>>= 6
      }
      utf8Buffer(0) = (0x100 - (1 << (8 - utf8Length)) + ucs4Char).toByte
    }
    utf8Length
  }

  private def uriError = UriError("Bad iri")

  private def encodeUnescaped(c: Char) = {
    if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9')) true
    else if ("-_.!~*'()".contains(c)) true
    else UriDecodeReserved.contains(c)
  }

  private val UriDecodeReserved = ";/?:@&=+$,#"

  case class UriError(message: String) extends Exception(message)
}

object RhinoEncoder {
  def apply(url: String): String = new RhinoEncoder().encode(url)
}
