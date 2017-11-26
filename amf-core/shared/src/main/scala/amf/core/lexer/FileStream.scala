package amf.core.lexer

import java.io.File

class FileStream(path: String, data: CharSequence) extends CharSequenceStream(path, data) {

  /** Creates an instance based on a file. */
  def this(file: File) = this(file.getPath, CharArraySequence(file))

  /** Creates an instance based on a file path. */
  def this(file: String) = this(new File(file))

  /** Creates an instance based on a file with a given encoding. */
  def this(file: File, encoding: String) = this(file.getPath, CharArraySequence(file, Some(encoding)))
}
