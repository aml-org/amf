package amf.apicontract.client.scala.common

import amf.core.client.scala.model.domain.AmfObject

object TypeUtil {

  def isTypeOf(element: AmfObject, typeIri: String): Boolean = {
    element.meta.`type`.map(_.iri()).contains(typeIri)
  }

}
