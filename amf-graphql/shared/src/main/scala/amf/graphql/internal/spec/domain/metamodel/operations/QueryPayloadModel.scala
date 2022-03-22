package amf.graphql.internal.spec.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Query, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain._
import amf.graphql.client.scala.model.domain.QueryPayload
import amf.shapes.client.scala.model.domain.operations.AbstractPayload
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractPayloadModel
import amf.shapes.internal.domain.metamodel.common.ExamplesField

object QueryPayloadModel extends AbstractPayloadModel {

  override val `type`: List[ValueType] = Query + "Payload" :: AbstractPayloadModel.`type`

  override def modelInstance = QueryPayload()

}
