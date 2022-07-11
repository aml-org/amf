package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidAbstractDeclarationType
import amf.core.client.scala.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.metamodel.domain.templates.{ParametrizedDeclarationModel, VariableValueModel}
import amf.core.internal.parser.YNodeLikeOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import org.yaml.model._

object ParametrizedDeclarationParser {
  def parse(producer: String => ParametrizedDeclaration)(node: YNode)(implicit
      ctx: WebApiContext
  ): ParametrizedDeclaration =
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
          case YType.Null =>
            val declaration = fromStringNode(entry.key)
            declaration.add(Annotations(entry))
          case _ =>
            val name = entry.key.as[YScalar].text
            val declaration =
              producer(name)
                .add(Annotations(entry))
            setName(declaration, name, entry.key)
            declaration.fields.setWithoutId(
              ParametrizedDeclarationModel.Target,
              declarations(name, SearchScope.Named),
              Annotations.inferred()
            )
            val variables = entry.value
              .as[YMap]
              .entries
              .zipWithIndex
              .map { case (variableEntry, index) =>
                val node = DataNodeParser(variableEntry.value).parse()
                VariableValue(variableEntry)
                  .withName(variableEntry.key)
                  .setWithoutId(VariableValueModel.Value, node, Annotations(variableEntry.value))
              }
            declaration.setWithoutId(
              ParametrizedDeclarationModel.Variables,
              AmfArray(variables, Annotations(entry.value)),
              Annotations.inferred()
            )
        }
      case _ if node.tagType == YType.Str =>
        val declaration = fromStringNode(node)
        declaration.add(Annotations.valueNode(node))
      case _ =>
        val declaration = producer("")
        declaration.add(Annotations(node))
        ctx.eh.violation(InvalidAbstractDeclarationType, declaration, "Invalid model extension.", node.location)
        declaration
    }
  }

  private def fromStringNode(node: YNode): ParametrizedDeclaration = {
    ctx.link(node) match {
      case Left(value) => // in oas links $ref always are maps
        producer(value)
          .setWithoutId(
            ParametrizedDeclarationModel.Target,
            declarations(value, SearchScope.Fragments)
              .link(ScalarNode(value), Annotations(node))
              .asInstanceOf[AbstractDeclaration]
          )
      case Right(n) =>
        val text = n.as[YScalar].text
        val target: AbstractDeclaration =
          declarations(text, SearchScope.All).link(ScalarNode(n), Annotations(n)).asInstanceOf[AbstractDeclaration]
        val parametrized = producer(text)
        setName(parametrized, text, n)
        parametrized
          .setWithoutId(ParametrizedDeclarationModel.Target, target, Annotations.inferred())
    }
  }

  def setName(declaration: ParametrizedDeclaration, name: String, key: YNode): Unit =
    declaration.setWithoutId(ParametrizedDeclarationModel.Name, AmfScalar(name, Annotations(key)), Annotations(key))
}
