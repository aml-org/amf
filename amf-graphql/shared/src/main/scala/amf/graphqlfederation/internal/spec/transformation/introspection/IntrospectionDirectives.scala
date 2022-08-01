package amf.graphqlfederation.internal.spec.transformation.introspection

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.graphql.internal.spec.parser.syntax.Locations.domainFor
import TypeBuilders.nullable
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
      .withDomain(domainFor(SCHEMA))
  }

  def `@requires`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("requires")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(FIELD_DEFINITION))
  }

  def `@provides`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("provides")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(FIELD_DEFINITION))
  }

  def `@key`(fieldSet: ScalarShape): CustomDomainProperty = {
    // TODO: 'repeatable is not modeled'
    CustomDomainProperty()
      .withName("key")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(OBJECT, INTERFACE))
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
