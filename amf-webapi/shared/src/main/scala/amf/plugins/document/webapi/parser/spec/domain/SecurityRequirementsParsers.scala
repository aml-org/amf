package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.VirtualObject
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, SearchScope}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.domain.webapi.metamodel.security.{
  OAuth2FlowModel,
  OpenIdConnectSettingsModel,
  ParametrizedSecuritySchemeModel,
  ScopeModel,
  SecurityRequirementModel
}
import amf.plugins.domain.webapi.models.security._
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validations.ParserSideValidations.{
  InvalidSecurityRequirementObject,
  ScopeNamesMustBeEmpty,
  UnknownScopeErrorSpecification
}
import org.yaml.model._

case class OasLikeSecurityRequirementParser(node: YNode,
                                            producer: String => SecurityRequirement,
                                            idCounter: IdCounter)(implicit val ctx: OasLikeWebApiContext) {
  def parse(): Option[SecurityRequirement] = node.to[YMap] match {
    case Right(map) if map.entries.nonEmpty =>
      val securityRequirement = producer(idCounter.genId("requirement")).add(Annotations(node))

      // Parse individual schemes
      map.entries.foreach { entry =>
        OasLikeParametrizedSecuritySchemeParser(entry, securityRequirement.withScheme).parse()
      }
      Some(securityRequirement)
    case Right(map) if map.entries.isEmpty =>
      None
    case _ =>
      val requirement = producer(node.toString).add(Annotations(node))
      ctx.eh.violation(InvalidSecurityRequirementObject, requirement.id, s"Invalid security requirement $node", node)
      Some(requirement)
  }

  case class OasLikeParametrizedSecuritySchemeParser(schemeEntry: YMapEntry,
                                                     producer: String => ParametrizedSecurityScheme) {
    def parse(): Option[ParametrizedSecurityScheme] = {

      val name   = schemeEntry.key.as[YScalar].text
      val scheme = producer(name).add(Annotations(schemeEntry))

      var declaration = parseTarget(name, scheme, schemeEntry)
      declaration = declaration.linkTarget match {
        case Some(d) => d.asInstanceOf[SecurityScheme]
        case None    => declaration
      }

      parseScopes(scheme, declaration, schemeEntry)

      Some(scheme)
    }

    private def parseScopes(scheme: ParametrizedSecurityScheme, declaration: SecurityScheme, schemeEntry: YMapEntry) = {
      if (declaration.`type`.is("OAuth 2.0")) {
        val settings = OAuth2Settings().adopted(scheme.id).add(Annotations(schemeEntry))
        val scopes   = getScopes(schemeEntry)
        val flows = Seq(
          settings
            .withFlow()
            .add(VirtualObject())
            .setArray(OAuth2FlowModel.Scopes, scopes, Annotations(schemeEntry.value)))

        scheme.scheme.settings match {
          case se: OAuth2Settings =>
            scopes.foreach(s => {
              if (!isValidScope(se.flows.headOption, s)) {
                ctx.eh.violation(
                  UnknownScopeErrorSpecification,
                  s.id,
                  Some(OAuth2FlowModel.Scopes.value.toString),
                  s"Scope '${s.name.value()}' not found in settings of declared secured by ${scheme.scheme.name.value()}.",
                  s.position(),
                  s.location()
                )
              }
            })
          case _ => //Nothing to do
        }

        scheme.set(ParametrizedSecuritySchemeModel.Settings, settings.withFlows(flows)).add(Annotations(schemeEntry))
      } else if (declaration.`type`.is("openIdConnect")) {
        val settings = OpenIdConnectSettings().adopted(scheme.id)
        val scopes   = getScopes(schemeEntry)
        scheme.set(ParametrizedSecuritySchemeModel.Settings,
                   settings.setArray(OpenIdConnectSettingsModel.Scopes, scopes, Annotations(schemeEntry.value)))
      } else if (schemeEntry.value.as[Seq[YNode]].nonEmpty) {
        val msg = declaration.`type`.option() match {
          case Some(schemeType) => s"Scopes array must be empty for security scheme type $schemeType"
          case None             => "Scopes array must be empty for given security scheme"
        }
        ctx.eh.violation(ScopeNamesMustBeEmpty, scheme.id, msg, node)
      }
    }

    private def isValidScope(maybeFlow: Option[OAuth2Flow], scope: Scope): Boolean =
      maybeFlow.exists(flow => flow.scopes.nonEmpty && flow.scopes.map(_.name.value()).contains(scope.name.value()))

    private def parseTarget(name: String, scheme: ParametrizedSecurityScheme, part: YPart): SecurityScheme = {
      ctx.declarations.findSecurityScheme(name, SearchScope.All) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)
          declaration
        case None =>
          val securityScheme = SecurityScheme()
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, securityScheme)
          ctx.eh.violation(DeclarationNotFound,
                           securityScheme.id,
                           s"Security scheme '$name' not found in declarations.",
                           part)
          securityScheme
      }
    }
  }

  private def getScopes(schemeEntry: YMapEntry) =
    schemeEntry.value
      .as[Seq[YNode]]
      .map(n => Scope(n).set(ScopeModel.Name, AmfScalar(n.as[String]), Annotations(n)))
}

object RamlSecurityRequirementParser {
  def parse(parentId: String, idCounter: IdCounter)(node: YNode)(
      implicit ctx: RamlWebApiContext): SecurityRequirement = {
    RamlSecurityRequirementParser(node, parentId, idCounter).parse()
  }
}
case class RamlSecurityRequirementParser(node: YNode, parentId: String, idCounter: IdCounter)(
    implicit val ctx: RamlWebApiContext) {
  def parse(): SecurityRequirement = {
    val requirement = SecurityRequirement(node).withSynthesizeName(idCounter.genId("default-requirement"))
    requirement.adopted(parentId)
    val scheme: ParametrizedSecurityScheme = RamlParametrizedSecuritySchemeParser(node, requirement.id).parse()
    requirement.set(SecurityRequirementModel.Schemes, AmfArray(Seq(scheme), Annotations(node)), Annotations.inferred())
  }

}
