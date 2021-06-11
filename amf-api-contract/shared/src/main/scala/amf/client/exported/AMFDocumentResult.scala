package amf.client.exported
import amf.client.convert.ApiClientConverters._
import amf.client.environment.{AMFDocumentResult => InternalAMFDocumentResult}
import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.Document

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AMFDocumentResult(private[amf] override val _internal: InternalAMFDocumentResult) extends AMFResult(_internal) {
  def document: Document = _internal.document
}
