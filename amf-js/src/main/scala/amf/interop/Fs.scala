package amf.interop

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@js.native
trait Fs extends js.Object {

  /** Asynchronously reads the entire contents of a file. */
  def readFile(file: String, callback: js.Function2[Any, Buffer, Any]): Unit = js.native

  /** Asynchronously writes data to a file, replacing the file if it already exists. data can be a string or a buffer. */
  def writeFile(file: String, data: Buffer | String, callback: js.Function1[Any, Any]): Unit = js.native
}

@js.native
@JSImport("fs", JSImport.Namespace, "fs")
object Fs extends Fs
