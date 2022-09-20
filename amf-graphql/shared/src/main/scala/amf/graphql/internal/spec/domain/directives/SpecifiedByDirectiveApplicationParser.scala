package amf.graphql.internal.spec.domain.directives

import amf.apicontract.internal.validation.definitions.ParserSideValidations.{InvalidArgumentValue, InvalidDirectiveLocation}
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DomainElement, ScalarNode}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.ValueParser
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, VALUE}
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.Node

class SpecifiedByDirectiveApplicationParser(override implicit val ctx: GraphQLBaseWebApiContext)
    extends DirectiveApplicationParser {
  override def appliesTo(node: Node): Boolean = isName("specifiedBy", node)

  override def parse(node: Node, element: DomainElement): Unit = {
    collectNodes(node, Seq(ARGUMENTS, ARGUMENT))
      .find(isName("url", _))
      .flatMap(pathToNonTerminal(_, Seq(VALUE)))
      .flatMap(ValueParser.parseValue(_)) match {
      case Some(scalarNode: ScalarNode) if scalarNode.dataType.value() == DataType.String =>
        setDataTypeField(element, scalarNode)
      case _ =>
        ctx.eh.violation(
          InvalidArgumentValue,
          element,
          "Argument 'url' on directive '@specifiedBy' must be of type string"
        )

    }
  }

  private def setDataTypeField(element: DomainElement, dataTypeNode: ScalarNode) = {
    element match {
      case scalarShape: ScalarShape =>
        scalarShape.withDataType(dataTypeNode.value.value())
      case _ =>
        ctx.eh.violation(
          InvalidDirectiveLocation,
          element,
          "Directive '@specifiedBy' can only be applied to Scalar type definitions"
        )
    }
  }
}
