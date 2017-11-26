package amf.core.interop

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** Operating System */
@js.native
trait OS extends js.Object {

  /** Returns the operating system's default directory for temporary files. */
  def tmpdir(): String = js.native
}

/** Operating System */
@js.native
@JSImport("os", JSImport.Namespace, "os")
object OS extends OS
