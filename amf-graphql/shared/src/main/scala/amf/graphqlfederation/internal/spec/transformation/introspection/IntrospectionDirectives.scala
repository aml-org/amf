package amf.graphqlfederation.internal.spec.transformation.introspection

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import TypeBuilders.nullable
import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper
import GraphQLLocationHelper.toLocationIris
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.ScalarNode
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}

object IntrospectionDirectives {

  private val FIELD_DEFINITION = "FIELD_DEFINITION"
  private val SCHEMA           = "SCHEMA"
  private val OBJECT           = "OBJECT"
  private val INTERFACE        = "INTERFACE"

  def `@external`(): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("external")
      .withSchema(NodeShape())
      .withDomain(toLocationIris(SCHEMA))
  }

  def `@requires`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("requires")
      .withSchema(fieldSetArgument(fieldSet))
      .withDomain(toLocationIris(FIELD_DEFINITION))
  }

  def `@provides`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("provides")
      .withSchema(fieldSetArgument(fieldSet))
      .withDomain(toLocationIris(FIELD_DEFINITION))
  }

  def `@key`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("key")
      .withRepeatable(true)
      .withSchema {
        NodeShape()
          .withProperties(
            List(
              PropertyShape()
                .withName("fieldSet")
                .withRange(fieldSet),
              PropertyShape()
                .withName("resolvable")
                .withDefault(ScalarNode("true", Some(DataTypes.Boolean)))
                .withRange(nullable(ScalarShape().withDataType(DataTypes.Boolean)))
            )
          )
      }
      .withDomain(toLocationIris(OBJECT, INTERFACE))
  }

  private def fieldSetArgument(fieldSet: ScalarShape): NodeShape = {
    NodeShape()
      .withProperties(
        List(
          PropertyShape()
            .withName("fieldSet")
            .withRange(fieldSet)
        )
      )
  }
}
