package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.internal.annotations.NullSecurity
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.shapes.internal.spec.contexts.parser.raml.RamlWebApiContext
import amf.plugins.domain.apicontract.metamodel.security._
import amf.plugins.domain.apicontract.models.security._
import amf.validations.ParserSideValidations.UnknownSecuritySchemeErrorSpecification
import org.yaml.model._

object RamlParametrizedSecuritySchemeParser {
  def parse(parentId: String)(node: YNode)(implicit ctx: RamlWebApiContext): ParametrizedSecurityScheme = {
    RamlParametrizedSecuritySchemeParser(node, parentId).parse()
  }
}

case class RamlParametrizedSecuritySchemeParser(node: YNode, parentId: String)(implicit ctx: RamlWebApiContext) {

  private val scheme: ParametrizedSecurityScheme = ParametrizedSecurityScheme(node)

  def parse(): ParametrizedSecurityScheme = node.tagType match {
    case YType.Null => scheme.withSynthesizeName("null").add(NullSecurity()).adopted(parentId)
    case YType.Map =>
      val schemeEntry = node.as[YMap].entries.head
      val name        = ScalarNode(schemeEntry.key)
      val nameText    = name.text().toString
      scheme.withName(name).adopted(parentId)

      ctx.declarations.findSecurityScheme(nameText, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations.inferred())

          val effectiveDeclaration =
            if (declaration.isLink)
              declaration.effectiveLinkTarget().asInstanceOf[SecurityScheme]
            else declaration

          val settings =
            RamlSecuritySettingsParser(schemeEntry.value, effectiveDeclaration.`type`.value(), scheme).parse()

          scheme.set(ParametrizedSecuritySchemeModel.Settings, settings, Annotations(schemeEntry))
        case None =>
          ctx.eh.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$nameText' not found in declarations (and name cannot be 'null').",
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
      scheme.withSynthesizeName("invalid").adopted(parentId)
    case _ =>
      val name: ScalarNode = ScalarNode(node)
      val textName         = name.text().toString
      scheme.withName(name).adopted(parentId)

      ctx.declarations.findSecurityScheme(textName, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.fields.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations.inferred())
          scheme
        case None =>
          ctx.eh.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$textName' not found in declarations.",
            node
          )
          scheme
      }
  }
}
