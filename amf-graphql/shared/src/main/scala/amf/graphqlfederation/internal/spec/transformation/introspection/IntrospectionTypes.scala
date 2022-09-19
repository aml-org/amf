package amf.graphqlfederation.internal.spec.transformation.introspection

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes
import amf.graphql.internal.spec.domain.model.{FieldBuilder, GraphqlArgument}
import TypeBuilders.{array, nullable}
import amf.core.internal.parser.domain.Annotations.virtual
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnionShape}

object IntrospectionTypes {

  def _Any(): ScalarShape = {
    ScalarShape()
      .withName("_Any")
      .withFormat("_Any")
      .withDataType(DataTypes.String)
  }

  def _FieldSet(): ScalarShape = {
    ScalarShape()
      .withName("_FieldSet")
      .withFormat("_FieldSet")
      .withDataType(DataTypes.Any)
  }

  def _Service(): NodeShape = {
    NodeShape()
      .withName("_Service")
      .withProperties(
        List(
          PropertyShape(virtual())
            .withName("sdl")
            .withRange(
              ScalarShape(virtual())
                .withDataType(DataTypes.String)
            )
        )
      )
  }

  def _Entity(types: Seq[NodeShape]): UnionShape = {
    val typesWithKey = types.filter(_.keys.nonEmpty)
    UnionShape()
      .withName("_Entity")
      .withAnyOf(typesWithKey)
  }

  def _Query(_any: AnyShape, _entity: Option[AnyShape], _serviceType: AnyShape): List[EndPoint] = {
    _entity match {
      case Some(e) =>
        List(
          _entities(_any, e),
          _service(_serviceType)
        )
      case None =>
        List(
          _service(_serviceType)
        )
    }
  }

  private def _service(_serviceType: AnyShape): EndPoint = {
    queryField("_service")
      .withSchema(_serviceType)
      .build()
  }

  private def _entities(_any: AnyShape, _entity: AnyShape): EndPoint = {
    queryField("_entities")
      .withArguments(
        List(
          GraphqlArgument("representations")
            .withSchema(array(_any))
            .withRequired(true)
        )
      )
      .withSchema(array(nullable(_entity)))
      .build()
  }

  private def queryField(name: String) = {
    FieldBuilder
      .empty()
      .withName(name)
      .withTypeName("Query")
      .withOperationType(RootTypes.Query)
  }
}
