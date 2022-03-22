package amf.graphql.internal.spec.domain.metamodel.operations

import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Query, Shapes}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{
  DomainElementModel,
  LinkableElementModel,
  ModelDoc,
  ModelVocabularies,
  ShapeModel
}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Str}
import amf.graphql.client.scala.model.domain.QueryParameter
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractParameterModel

object QueryParameterModel extends AbstractParameterModel {

  override val key: Field = Name

  override val `type`: List[ValueType] = Query + "Parameter" :: AbstractParameterModel.`type`

  override def modelInstance = QueryParameter()

}
