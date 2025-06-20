package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.domain.RamlParametrizedSecuritySchemeParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  InvalidSecurityRequirementObject,
  ScopeNamesMustBeEmpty,
  UnknownScopeErrorSpecification
}
import amf.core.client.scala.model.domain.{AmfArray, AmfObject}
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.utils.IdCounter
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import org.yaml.model._

case class OasLikeSecurityRequirementParser(node: YNode, adopted: SecurityRequirement => Unit, idCounter: IdCounter)(
    implicit val ctx: OasLikeWebApiContext
) {
  def parse(): Option[SecurityRequirement] = node.to[YMap] match {
    case Right(map) if map.entries.nonEmpty =>
      val securityRequirement =
        SecurityRequirement(Annotations(node)).withSynthesizeName(idCounter.genId("requirement"))
      adopted(securityRequirement)
      // Parse individual schemes
      val schemes = map.entries.flatMap { entry =>
        OasLikeParametrizedSecuritySchemeParser(entry, p => Unit).parse()
      }
      securityRequirement.setWithoutId(
        SecurityRequirementModel.Schemes,
        AmfArray(schemes, Annotations(map)),
        Annotations.inferred()
      )
      Some(securityRequirement)
    case Right(map) if map.entries.isEmpty =>
      None
    case _ =>
      val requirement = SecurityRequirement(Annotations(node)).withName(ScalarNode(node))
      adopted(requirement)

      ctx.eh.violation(
        InvalidSecurityRequirementObject,
        requirement,
        s"Invalid security requirement $node",
        node.location
      )
      Some(requirement)
  }

  private case class OasLikeParametrizedSecuritySchemeParser(
      schemeEntry: YMapEntry,
      adopted: ParametrizedSecurityScheme => Unit
  ) {
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

      parseScopes(scheme, declaration)

      Some(scheme)
    }

    private def parseScopes(scheme: ParametrizedSecurityScheme, declaration: SecurityScheme): Unit = {
      declaration.`type`.option() match {
        case Some("OAuth 2.0")     => parseOAuth2Scopes(scheme)
        case Some("openIdConnect") => parseOpenIdConnectScopes(scheme)
        // OAS 3.1 added optional scopes for any security type
        case Some("Api Key") if nonEmptyScopes() && ctx.isOas31Context   => parseApiKeyScopes(scheme)
        case Some("http") if nonEmptyScopes() && ctx.isOas31Context      => parseHttpScopes(scheme)
        case Some("mutualTLS") if nonEmptyScopes() && ctx.isOas31Context => parseMutualTLSScopes(scheme)
        case _ if nonEmptyScopes()                                       => scopesError(scheme, declaration)
        case _                                                           => // No scopes, so nothing to do
      }
    }

    private def parseOAuth2Scopes(scheme: ParametrizedSecurityScheme): Unit = {
      val settings         = OAuth2Settings(Annotations(schemeEntry))
      val scopes           = getScopes(schemeEntry)
      val flow: OAuth2Flow = OAuth2Flow(Annotations.virtual())

      flow.fields.setWithoutId(
        OAuth2FlowModel.Scopes,
        AmfArray(scopes, Annotations(schemeEntry.value)),
        Annotations(schemeEntry)
      )
      val flows = Seq(flow)

      scheme.scheme.settings match {
        case se: OAuth2Settings =>
          scopes.foreach(s => {
            if (!isValidScope(se.flows, s)) {
              ctx.eh.violation(
                UnknownScopeErrorSpecification,
                s,
                Some(OAuth2FlowModel.Scopes.value.toString),
                s"Scope '${s.name.value()}' not found in settings of declared secured by ${scheme.scheme.name.value()}.",
                s.position(),
                s.location()
              )
            }
          })
        case _ => // Nothing to do
      }

      scheme
        .setWithoutId(ParametrizedSecuritySchemeModel.Settings, settings.withFlows(flows), Annotations.inferred())
        .add(Annotations(schemeEntry))
    }

    private def parseOpenIdConnectScopes(scheme: ParametrizedSecurityScheme): Unit = {
      val settings = OpenIdConnectSettings()
      setScopes(scheme, settings)
    }

    private def parseApiKeyScopes(scheme: ParametrizedSecurityScheme): Unit = {
      val settings = ApiKeySettings()
      setScopes(scheme, settings)
    }

    private def parseHttpScopes(scheme: ParametrizedSecurityScheme): Unit = {
      val settings = HttpSettings()
      setScopes(scheme, settings)
    }

    private def parseMutualTLSScopes(scheme: ParametrizedSecurityScheme): Unit = {
      val settings = MutualTLSSettings()
      setScopes(scheme, settings)
    }

    private def setScopes(scheme: ParametrizedSecurityScheme, settings: AmfObject): Unit = {
      val scopes = getScopes(schemeEntry)
      scheme.setWithoutId(
        ParametrizedSecuritySchemeModel.Settings,
        settings.setArray(OpenIdConnectSettingsModel.Scopes, scopes, Annotations(schemeEntry.value)),
        Annotations.inferred()
      )
    }

    private def scopesError(scheme: ParametrizedSecurityScheme, declaration: SecurityScheme): Unit = {
      val msg = declaration.`type`.option() match {
        case Some(schemeType) => s"Scopes array must be empty for security scheme type $schemeType"
        case None             => "Scopes array must be empty for given security scheme"
      }
      ctx.eh.violation(ScopeNamesMustBeEmpty, scheme, msg, node.location)
    }

    private def nonEmptyScopes(): Boolean = schemeEntry.value.as[Seq[YNode]].nonEmpty

    private def isValidScope(flows: Seq[OAuth2Flow], scope: Scope): Boolean =
      flows.exists(flow => flow.scopes.nonEmpty && flow.scopes.map(_.name.value()).contains(scope.name.value()))

    private def parseTarget(name: String, scheme: ParametrizedSecurityScheme, part: YPart): SecurityScheme = {
      ctx.declarations.findSecurityScheme(name, SearchScope.All) match {
        case Some(declaration) =>
          scheme.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations.inferred())
          declaration
        case None =>
          val securityScheme = SecurityScheme(Annotations.virtual())
          scheme.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, securityScheme, Annotations.synthesized())
          ctx.eh.violation(
            DeclarationNotFound,
            securityScheme,
            s"Security scheme '$name' not found in declarations.",
            part.location
          )
          securityScheme
      }
    }
  }

  private def getScopes(schemeEntry: YMapEntry) =
    schemeEntry.value
      .as[Seq[YNode]]
      .map(n => Scope(n).setWithoutId(ScopeModel.Name, ScalarNode(n).text(), Annotations.inferred()))
}

object RamlSecurityRequirementParser {
  def parse(parentId: String, idCounter: IdCounter)(
      node: YNode
  )(implicit ctx: RamlWebApiContext): SecurityRequirement = {
    RamlSecurityRequirementParser(node, parentId, idCounter).parse()
  }
}
case class RamlSecurityRequirementParser(node: YNode, parentId: String, idCounter: IdCounter)(implicit
    val ctx: RamlWebApiContext
) {
  def parse(): SecurityRequirement = {
    val requirement = SecurityRequirement(node).withSynthesizeName(idCounter.genId("default-requirement"))
    val scheme: ParametrizedSecurityScheme = RamlParametrizedSecuritySchemeParser(node, requirement.id).parse()
    requirement.setWithoutId(
      SecurityRequirementModel.Schemes,
      AmfArray(Seq(scheme), Annotations(node)),
      Annotations.inferred()
    )
  }

}
