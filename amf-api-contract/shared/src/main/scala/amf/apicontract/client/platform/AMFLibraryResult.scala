package amf.apicontract.client.platform

import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.Module

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFLibraryResult(private[amf] override val _internal: AMFLibraryResult) extends AMFResult(_internal) {
  def library: Module = _internal.library
}
