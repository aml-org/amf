package amf.graphql.internal.spec.domain.model

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized}
import amf.graphql.internal.spec.document._

object GraphqlArgument {

  def apply(annotations: Annotations, fieldName: AmfScalar): Parameter = {
    val param = Parameter(annotations).withName(fieldName.toString(), fieldName.annotations)
    param synthetically () set "query" as ParameterModel.Binding
  }

  def apply(name: String): Parameter = {
    val param = Parameter().withName(name, inferred())
    param synthetically () set "query" as ParameterModel.Binding
  }
}
