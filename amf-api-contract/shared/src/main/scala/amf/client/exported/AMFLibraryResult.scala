package amf.client.exported
import amf.client.environment.{AMFLibraryResult => InternalAMFLibraryResult}
import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.Module
import amf.core.internal.convert.CoreClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFLibraryResult(private[amf] override val _internal: InternalAMFLibraryResult) extends AMFResult(_internal) {
  def library: Module = _internal.library
}
