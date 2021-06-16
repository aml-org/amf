package amf.apicontract.client.platform

import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.Module
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.{AMFLibraryResult => InternalAMFLibraryResult}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFLibraryResult(private[amf] override val _internal: InternalAMFLibraryResult) extends AMFResult(_internal) {
  def library: Module = _internal.library
}
