package amf.plugins.document.webapi.parser.spec.domain

import amf.core.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.validations.ParserSideValidations.InvalidAbstractDeclarationType
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
    node.toOption[YMap].flatMap(_.entries.headOption) match {
      case Some(entry) =>
        entry.value.tagType match {
          case YType.Null => fromStringNode(entry.key)
          case _ =>
            val name = entry.key.as[YScalar].text
            val declaration =
              producer(name)
                .add(Annotations.valueNode(node))
            setName(declaration, name, entry.key)
            declaration.fields.setWithoutId(ParametrizedDeclarationModel.Target, declarations(name, SearchScope.Named))
            val variables = entry.value
              .as[YMap]
              .entries
              .zipWithIndex
              .map {
                case (variableEntry, index) =>
                  val node = DataNodeParser(variableEntry.value, parent = Some(s"${declaration.id}_$index")).parse()
                  VariableValue(variableEntry)
                    .withName(variableEntry.key.as[YScalar].text)
                    .withValue(node)
              }
            declaration.withVariables(variables)
        }
      case _ if node.tagType == YType.Str => fromStringNode(node)
      case _ =>
        val declaration = producer("")
        ctx.eh.violation(InvalidAbstractDeclarationType, declaration.id, "Invalid model extension.", node)
        declaration
    }
  }

  private def fromStringNode(node: YNode): ParametrizedDeclaration = {
    ctx.link(node) match {
      case Left(value) => // in oas links $ref always are maps
        producer(value)
          .add(Annotations.valueNode(node))
          .set(ParametrizedDeclarationModel.Target,
               declarations(value, SearchScope.Fragments).link(value).asInstanceOf[AbstractDeclaration])
      case Right(n) =>
        val text         = n.as[YScalar].text
        val target       = declarations(text, SearchScope.All).link(text).asInstanceOf[AbstractDeclaration]
        val parametrized = producer(text)
        setName(parametrized, text, n)
        parametrized
          .add(Annotations.valueNode(node))
          .set(ParametrizedDeclarationModel.Target, target)
    }
  }

  def setName(declaration: ParametrizedDeclaration, name: String, key: YNode): Unit =
    declaration.set(ParametrizedDeclarationModel.Name, AmfScalar(name), Annotations(key))
}
