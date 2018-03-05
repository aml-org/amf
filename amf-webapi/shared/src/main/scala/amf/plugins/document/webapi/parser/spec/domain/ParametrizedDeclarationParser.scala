package amf.plugins.document.webapi.parser.spec.domain

import amf.core.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.core.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import org.yaml.model._

object ParametrizedDeclarationParser {
  def parse(producer: String => ParametrizedDeclaration)(node: YNode)(
      implicit ctx: WebApiContext): ParametrizedDeclaration =
    ParametrizedDeclarationParser(node, producer, ctx.declarations.findTraitOrError(node)).parse()
}

case class ParametrizedDeclarationParser(
    node: YNode,
    producer: String => ParametrizedDeclaration,
    declarations: (String, SearchScope.Scope) => AbstractDeclaration
)(implicit ctx: WebApiContext) {
  def parse(): ParametrizedDeclaration = {
    node.tagType match {
      case YType.Map =>
        // TODO is it always the first child?
        val entry = node.as[YMap].entries.head

        entry.value.tagType match {
          case YType.Null => fromStringNode(entry.key)
          case _ =>
            val name = entry.key.as[YScalar].text
            val declaration =
              producer(name)
                .add(Annotations(node.value))
            declaration.fields.setWithoutId(ParametrizedDeclarationModel.Target, declarations(name, SearchScope.Named))
            val variables = entry.value
              .as[YMap]
              .entries
              .map { variableEntry =>
                val node = DataNodeParser(variableEntry.value, parent = Some(declaration.id)).parse()
                VariableValue(variableEntry)
                  .withName(variableEntry.key.as[YScalar].text)
                  .withValue(node)
              }
            declaration.withVariables(variables)
        }

      case YType.Str => fromStringNode(node)
      case _ =>
        val declaration = producer("") // todo : review with pedro
        ctx.violation(declaration.id, "Invalid model extension.", node)
        declaration
    }
  }

  private def fromStringNode(node: YNode): ParametrizedDeclaration = {
    ctx.link(node) match {
      case Left(value) => // in oas links $ref always are maps
        producer(value)
          .add(Annotations(node.value))
          .set(ParametrizedDeclarationModel.Target, declarations(value, SearchScope.Fragments))
      case Right(n) =>
        val text = n.as[YScalar].text
        producer(text)
          .add(Annotations(node.value))
          .set(ParametrizedDeclarationModel.Target, declarations(text, SearchScope.All))
    }
  }
}
