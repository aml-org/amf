package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape, UnionShape}

case class GraphQLObject(node: NodeShape) {
  def name: String                      = node.name.value()
  def isInterface: Boolean              = node.isAbstract.value()
  def isInput: Boolean                  = node.isInputOnly.value()
  def isExtensionWrapper: Boolean       = node.and.nonEmpty
  def isSchema: Boolean                 = node.name.isNullOrEmpty
  def properties: Seq[GraphQLProperty]  = node.properties.map(GraphQLProperty)
  def operations: Seq[GraphQLOperation] = node.operations.map(GraphQLOperation)

  def fields(): GraphQLFields = GraphQLFields(properties, operations)

}

case class GraphQLFields(properties: Seq[GraphQLProperty], operations: Seq[GraphQLOperation]) {
  def names: Set[String] = {
    (properties.map(_.name) ++ operations.map(_.name)).toSet
  }

}

case class GraphQLProperty(property: PropertyShape) {
  def name: String             = property.name.value()
  def datatype: Option[String] = GraphQLUtils.datatype(property.range)
  def default: DataNode        = { property.default }

}

case class GraphQLOperation(operation: ShapeOperation) {
  def name: String                      = operation.name.value()
  def parameters: Seq[GraphQLParameter] = operation.requests.flatMap(_.queryParameters).map(GraphQLParameter)
}

case class GraphQLParameter(parameter: ShapeParameter) {
  def name: String             = parameter.name.value()
  def datatype: Option[String] = GraphQLUtils.datatype(parameter.schema)
  def default: DataNode        = parameter.schema.default
}

case class GraphQLAppliedDirective(directive: DomainExtension) {
  def name: String = directive.name.value()

  def definedProps(): Seq[GraphQLProperty] = directive.definedBy match {
    case c: CustomDomainProperty =>
      c.schema match {
        case n: NodeShape => n.properties.map(GraphQLProperty)
        case _            => Seq()
      }
    case _ => Seq()
  }

  def parsedProps(): Seq[ScalarNode] = directive.extension match {
    case o: ObjectNode => o.allProperties().map(_.asInstanceOf[ScalarNode]).toList
    case _             => Seq()
  }
}

object GraphQLUtils {
  def datatype(shape: Shape): Option[String] = {
    shape match {
      case u: UnionShape => // nullable type
        u.anyOf.collectFirst { case s: ScalarShape => s.dataType.value() }
      case s: ScalarShape => Some(s.dataType.value())
      case _              => None
    }
  }
}
