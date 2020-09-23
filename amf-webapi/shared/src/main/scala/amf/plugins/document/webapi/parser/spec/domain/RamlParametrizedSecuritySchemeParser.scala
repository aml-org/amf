package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.NullSecurity
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import amf.validations.ParserSideValidations.UnknownSecuritySchemeErrorSpecification
import org.yaml.model._

object RamlParametrizedSecuritySchemeParser {
  def parse(producer: String => ParametrizedSecurityScheme)(node: YNode)(
      implicit ctx: RamlWebApiContext): ParametrizedSecurityScheme = {
    RamlParametrizedSecuritySchemeParser(node, producer).parse()
  }
}

case class RamlParametrizedSecuritySchemeParser(node: YNode, producer: String => ParametrizedSecurityScheme)(
    implicit ctx: RamlWebApiContext) {
  def parse(): ParametrizedSecurityScheme = node.tagType match {
    case YType.Null => producer("null").add(Annotations(node) += NullSecurity())
    case YType.Map =>
      val schemeEntry = node.as[YMap].entries.head
      val name        = schemeEntry.key.as[YScalar].text
      val scheme      = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)

          val effectiveDeclaration =
            if (declaration.isLink)
              declaration.effectiveLinkTarget().asInstanceOf[SecurityScheme]
            else declaration

          val settings =
            RamlSecuritySettingsParser(schemeEntry.value, effectiveDeclaration.`type`.value(), scheme).parse()

          scheme.set(ParametrizedSecuritySchemeModel.Settings, settings)
        case None =>
          ctx.eh.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$name' not found in declarations (and name cannot be 'null').",
            node
          )
      }

      scheme
    case YType.Include =>
      ctx.eh.violation(
        UnknownSecuritySchemeErrorSpecification,
        "",
        "'securedBy' property doesn't accept !include tag, only references to security schemes.",
        node
      )
      producer("invalid").add(Annotations(node))
    case _ =>
      val name: String = node.as[YScalar].text
      val scheme       = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.fields.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations())
          scheme
        case None =>
          ctx.eh.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$name' not found in declarations.",
            node
          )
          scheme
      }
  }
}
