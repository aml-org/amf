package amf.model

trait EncodesModel {

  private[amf] val element: amf.document.EncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  lazy val encodes: DomainElement = element.encodes match {
    case api: amf.domain.WebApi => WebApi(api)
  }
}
