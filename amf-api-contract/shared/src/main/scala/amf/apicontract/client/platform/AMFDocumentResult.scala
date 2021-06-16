package amf.apicontract.client.platform

import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.Document
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.{AMFDocumentResult => InternalAMFDocumentResult}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFDocumentResult(private[amf] override val _internal: InternalAMFDocumentResult) extends AMFResult(_internal) {
  def document: Document = _internal.document
}
