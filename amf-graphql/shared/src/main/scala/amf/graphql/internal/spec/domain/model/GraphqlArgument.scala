package amf.graphql.internal.spec.domain.model

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized}

object GraphqlArgument {

  def apply(annotations: Annotations, fieldName: AmfScalar): Parameter = {
    Parameter(annotations)
      .withName(fieldName.toString(), fieldName.annotations)
      .set(ParameterModel.Binding, "query", synthesized())
  }

  def apply(name: String): Parameter = {
    Parameter()
      .withName(name, inferred())
      .set(ParameterModel.Binding, "query", synthesized())
  }
}
