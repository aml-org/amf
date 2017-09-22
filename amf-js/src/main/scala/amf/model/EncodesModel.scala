package amf.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait EncodesModel {

  /** Encoded [[DomainElement]] described in the document element. */
  private[amf] val element: amf.document.EncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  lazy val encodes: DomainElement = element.encodes match {
    case api: amf.domain.WebApi => WebApi(api)
  }
}
