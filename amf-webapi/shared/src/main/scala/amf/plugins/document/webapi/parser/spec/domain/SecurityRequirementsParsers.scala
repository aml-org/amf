package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, SearchScope}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.{OasWebApiContext, WebApiContext}
import amf.plugins.domain.webapi.metamodel.security.{OAuth2FlowModel, ParametrizedSecuritySchemeModel, ScopeModel}
import amf.plugins.domain.webapi.models.security._
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validations.ParserSideValidations.{InvalidSecurityRequirementObject, ScopeNamesMustBeEmpty}
import org.yaml.model._

case class OasSecurityRequirementParser(node: YNode, producer: String => SecurityRequirement, idCounter: IdCounter)(
    implicit val ctx: OasWebApiContext) {
  def parse(): Option[SecurityRequirement] = node.to[YMap] match {
    case Right(map) if map.entries.nonEmpty =>
      val securityRequirement = producer(idCounter.genId("requirement"))

      // Parse individual schemes
      map.entries.foreach { entry =>
        OasParametrizedSecuritySchemeParser(entry, securityRequirement.withScheme).parse()
      }
      Some(securityRequirement)
    case Right(map) if map.entries.isEmpty =>
      None
    case _ =>
      val requirement = producer(node.toString)
      ctx.violation(InvalidSecurityRequirementObject, requirement.id, s"Invalid security requirement $node", node)
      None
  }

  case class OasParametrizedSecuritySchemeParser(schemeEntry: YMapEntry,
                                                 producer: String => ParametrizedSecurityScheme) {
    def parse(): Option[ParametrizedSecurityScheme] = {

      val name   = schemeEntry.key.as[YScalar].text
      val scheme = producer(name).add(Annotations(schemeEntry))

      var declaration = parseTarget(name, scheme, schemeEntry)
      declaration = declaration.linkTarget match {
        case Some(d) => d.asInstanceOf[SecurityScheme]
        case None    => declaration
      }

      if (declaration.`type`.is("OAuth 2.0")) {
        val settings = OAuth2Settings().adopted(scheme.id)
        val scopes = schemeEntry.value
          .as[Seq[YNode]]
          .map(n => Scope(n).set(ScopeModel.Name, AmfScalar(n.as[String]), Annotations(n)))
        val flows = Seq(
          settings
            .withFlow()
            .setArray(OAuth2FlowModel.Scopes, scopes, Annotations(schemeEntry.value)))

        scheme.set(ParametrizedSecuritySchemeModel.Settings, settings.withFlows(flows))
      }

      validateScopesArray(scheme, declaration, schemeEntry)
      Some(scheme)
    }

    private def validateScopesArray(scheme: ParametrizedSecurityScheme,
                                    declaration: SecurityScheme,
                                    schemeEntry: YMapEntry): Unit = {
      if (declaration.`type`.nonEmpty &&
          !(declaration.`type`.is("OAuth 2.0") || declaration.`type`.is("openIdConnect"))) {
        schemeEntry.value.tag.tagType match {
          case YType.Seq if schemeEntry.value.as[Seq[YNode]].nonEmpty =>
            val msg = declaration.`type`.option() match {
              case Some(schemeType) => s"Scopes array must be empty for security scheme type $schemeType"
              case None             => "Scopes array must be empty for given security scheme"
            }
            ctx.violation(ScopeNamesMustBeEmpty, scheme.id, msg, node)
          case _ =>
        }
      }
    }

    private def parseTarget(name: String, scheme: ParametrizedSecurityScheme, part: YPart): SecurityScheme = {
      ctx.declarations.findSecurityScheme(name, SearchScope.All) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)
          declaration
        case None =>
          val securityScheme = SecurityScheme()
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, securityScheme)
          ctx.violation(DeclarationNotFound,
                        securityScheme.id,
                        s"Security scheme '$name' not found in declarations.",
                        part)
          securityScheme
      }
    }
  }
}

object RamlSecurityRequirementParser {
  def parse(producer: String => SecurityRequirement)(node: YNode)(implicit ctx: WebApiContext): SecurityRequirement = {
    RamlSecurityRequirementParser(node, producer).parse()
  }
}
case class RamlSecurityRequirementParser(node: YNode, producer: String => SecurityRequirement)(
    implicit val ctx: WebApiContext) {
  def parse(): SecurityRequirement = {
    val requirement = producer("default-requirement").add(Annotations(node))
    RamlParametrizedSecuritySchemeParser(node, requirement.withScheme).parse()
    requirement
  }

}
