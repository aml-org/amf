package amf.graphql.internal.spec.domain.model

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations

object GraphqlArgument {

  def apply(annotations: Annotations, fieldName: AmfScalar) = {
    Parameter(annotations).withName(fieldName.toString(), fieldName.annotations).withBinding("query")
  }

  def apply(name: String) = {
    Parameter().withName(name).withBinding("query")
  }
}
