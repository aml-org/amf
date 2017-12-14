package amf.core.lexer

import java.io._

/**
  *
  */
final class CharArraySequence(val data: Array[Char], val offset: Int, val length: Int) extends CharSequence {

  def this(data: Array[Char]) = this(data, 0, data.length)

  override def charAt(index: Int): Char = {
    if ((index < 0) || (index >= length)) throw new IndexOutOfBoundsException("index: " + index)
    data(offset + index)
  }

  override def subSequence(start: Int, end: Int): CharSequence = {
    if (start < 0 || start > end || end > length) throw new IndexOutOfBoundsException
    new CharArraySequence(data, offset + start, end - start)
  }

  // Missing eq and hash

  override def toString = new String(data, offset, length)
}

object CharArraySequence {
  def apply(file: File, encoding: Option[String] = None): CharArraySequence =
    apply(new FileInputStream(file), file.length.toInt, encoding)

  def apply(stream: InputStream, length: Int, encoding: Option[String]): CharArraySequence = {
    val map: Option[InputStreamReader] = encoding.map(e => new InputStreamReader(stream, e))
    val isr: InputStreamReader         = map.getOrElse(new InputStreamReader(stream))
    try {
      val buf = Stream.continually(isr.read).takeWhile(_ != -1).map(_.toChar).toArray
      new CharArraySequence(buf, 0, buf.length)
    } finally {
      Option(isr).foreach(_.close())
    }
  }
}
