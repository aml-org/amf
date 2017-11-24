package amf.model

import amf.framework.model.domain
import amf.plugins.document.vocabularies.model
import amf.plugins.domain.webapi.models

trait EncodesModel {

  private[amf] val element: amf.framework.model.document.EncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  lazy val encodes: DomainElement = element.encodes match {
    case api: models.WebApi => WebApi(api)
    case entity: model.domain.DomainEntity => DomainEntity(entity)
  }
}
