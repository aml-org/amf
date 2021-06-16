package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.domain.RamlParametrizedSecuritySchemeParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidSecurityRequirementObject
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.utils.IdCounter
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import org.yaml.model._

case class OasLikeSecurityRequirementParser(node: YNode, adopted: SecurityRequirement => Unit, idCounter: IdCounter)(
    implicit val ctx: OasLikeWebApiContext) {
  def parse(): Option[SecurityRequirement] = node.to[YMap] match {
    case Right(map) if map.entries.nonEmpty =>
      val securityRequirement =
        SecurityRequirement(Annotations(node)).withSynthesizeName(idCounter.genId("requirement"))
      adopted(securityRequirement)
      // Parse individual schemes
      val schemes = map.entries.flatMap { entry =>
        OasLikeParametrizedSecuritySchemeParser(entry, p => p.adopted(securityRequirement.id)).parse()
      }
      securityRequirement.set(SecurityRequirementModel.Schemes,
                              AmfArray(schemes, Annotations(map)),
                              Annotations.inferred())
      Some(securityRequirement)
    case Right(map) if map.entries.isEmpty =>
      None
    case _ =>
      val requirement = SecurityRequirement(Annotations(node)).withName(ScalarNode(node))
      adopted(requirement)

      ctx.eh.violation(InvalidSecurityRequirementObject, requirement.id, s"Invalid security requirement $node", node)
      Some(requirement)
  }

  case class OasLikeParametrizedSecuritySchemeParser(schemeEntry: YMapEntry,
                                                     adopted: ParametrizedSecurityScheme => Unit) {
    def parse(): Option[ParametrizedSecurityScheme] = {

      val name = schemeEntry.key.asScalar.map(_.text)

      val scheme = ParametrizedSecurityScheme(Annotations(schemeEntry))
      name.map(n => scheme.withName(n, Annotations(schemeEntry.key)))
      adopted(scheme)
      var declaration = parseTarget(name.getOrElse(""), scheme, schemeEntry)
      declaration = declaration.linkTarget match {
        case Some(d) => d.asInstanceOf[SecurityScheme]
        case None    => declaration
      }

      parseScopes(scheme, declaration, schemeEntry)

      Some(scheme)
    }

    private def parseScopes(scheme: ParametrizedSecurityScheme, declaration: SecurityScheme, schemeEntry: YMapEntry) = {
      if (declaration.`type`.is("OAuth 2.0")) {
        val settings = OAuth2Settings(Annotations(schemeEntry)).adopted(scheme.id)
        val scopes   = getScopes(schemeEntry)
        val flow: OAuth2Flow = OAuth2Flow(Annotations.virtual())
          .adopted(settings.id)
        flow.fields.set(flow.id,
                        OAuth2FlowModel.Scopes,
                        AmfArray(scopes, Annotations(schemeEntry.value)),
                        Annotations(schemeEntry))
        val flows = Seq(flow)

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

        scheme
          .set(ParametrizedSecuritySchemeModel.Settings, settings.withFlows(flows), Annotations.inferred())
          .add(Annotations(schemeEntry))
      } else if (declaration.`type`.is("openIdConnect")) {
        val settings = OpenIdConnectSettings().adopted(scheme.id)
        val scopes   = getScopes(schemeEntry)
        scheme.set(
          ParametrizedSecuritySchemeModel.Settings,
          settings.setArray(OpenIdConnectSettingsModel.Scopes, scopes, Annotations(schemeEntry.value)),
          Annotations.inferred()
        )
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
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations.inferred())
          declaration
        case None =>
          val securityScheme = SecurityScheme(Annotations.virtual())
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, securityScheme, Annotations.synthesized())
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
      .map(n => Scope(n).set(ScopeModel.Name, ScalarNode(n).text(), Annotations.inferred()))
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
