package amf.client.exported
import amf.client.environment.{AMFLibraryResult => InternalAMFLibraryResult}
import amf.client.convert.WebApiClientConverters._
import amf.client.model.document.Module

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFLibraryResult(private[amf] override val _internal: InternalAMFLibraryResult) extends AMFResult(_internal) {
  def library: Module = _internal.library
}
