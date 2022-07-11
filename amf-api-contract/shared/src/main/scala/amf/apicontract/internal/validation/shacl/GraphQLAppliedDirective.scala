package amf.apicontract.internal.validation.shacl

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{ObjectNode, ScalarNode}
import amf.shapes.client.scala.model.domain.NodeShape

case class GraphQLAppliedDirective(directive: DomainExtension) {

  def definedProps(): Seq[PropertyShape] = directive.definedBy match {
    case c: CustomDomainProperty =>
      c.schema match {
        case n: NodeShape => n.properties
        case _            => Seq()
      }
    case _ => Seq()
  }

  def parsedProps(): Seq[ScalarNode] = directive.extension match {
    case o: ObjectNode => o.allProperties().map(_.asInstanceOf[ScalarNode]).toList
    case _             => Seq()
  }
}

case class GraphQLArgument(property: PropertyShape) {}
