package amf.spec.domain

import amf.domain.Annotations
import amf.domain.`abstract`.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel
import org.yaml.model._
import amf.parser.YScalarYRead
import amf.spec.{ParserContext, SearchScope}

/**
  *
  */
case class ParametrizedDeclarationParser(
    node: YNode,
    producer: String => ParametrizedDeclaration,
    declarations: (String, SearchScope.Scope) => AbstractDeclaration)(implicit ctx: ParserContext) {
  def parse(): ParametrizedDeclaration = {
    node.tagType match {
      case YType.Map =>
        // TODO is it always the first child?
        val entry = node.as[YMap].entries.head

        val name = entry.key.as[YScalar].text
        val declaration =
          producer(name)
            .add(Annotations(node.value))
            .set(ParametrizedDeclarationModel.Target, declarations(name, SearchScope.Named).id)
        val variables = entry.value
          .as[YMap]
          .entries
          .map(
            variableEntry =>
              VariableValue(variableEntry)
                .withName(variableEntry.key.as[YScalar].text)
                .withValue(variableEntry.value.as[YScalar].text))

        declaration.withVariables(variables)
      case YType.Str =>
        ctx.link(node) match {
          case Left(value) => // in oas links $ref always are maps
            producer(value)
              .add(Annotations(node.value))
              .set(ParametrizedDeclarationModel.Target, declarations(value, SearchScope.Fragments).id)
          case Right(n) =>
            val text = n.as[YScalar].text
            producer(text)
              .add(Annotations(node.value))
              .set(ParametrizedDeclarationModel.Target, declarations(text, SearchScope.All).id)
        }

      case _ =>
        val declaration = producer("") // todo : review with pedro
        ctx.violation(declaration.id, "Invalid model extension.", node)
        declaration
    }
  }
}
