package amf.model

import amf.plugins.domain.webapi.models

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait EncodesModel {

  /** Encoded [[DomainElement]] described in the document element. */
  private[amf] val element: amf.framework.model.document.EncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  lazy val encodes: DomainElement = element.encodes match {
    case api: models.WebApi => WebApi(api)
  }
}
