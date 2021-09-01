package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.spec.common.parser.WellKnownAnnotation.isRamlAnnotation
import amf.apicontract.internal.spec.common.parser.{
  SpecField,
  SpecNode,
  SpecParserOps,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  MissingRequiredFieldForGrantType,
  UnknownScopeErrorSpecification
}
import amf.core.client.scala.model.domain.{AmfArray, DomainElement}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, Lazy}
import amf.shapes.internal.spec.common.parser.AnnotationParser
import amf.shapes.internal.spec.datanode.DataNodeParser
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable

case class RamlSecuritySettingsParser(node: YNode, `type`: String, scheme: DomainElement with WithSettings)(
    implicit val ctx: RamlWebApiContext)
    extends SpecParserOps {

  protected val map: YMap = node.as[YMap]
  def parse(): Settings = {
    val result = `type` match {
      case "OAuth 1.0"   => oauth1()
      case "OAuth 2.0"   => oauth2()
      case `apiKeyConst` => apiKey()
      case _             => dynamicSettings(scheme.withDefaultSettings())
    }

    AnnotationParser(result, map, List(VocabularyMappings.securitySettings))(WebApiShapeParserContextAdapter(ctx))
      .parse()

    result.add(Annotations(node))
  }

  val apiKeyConst: String = "apiKey".asOasExtension

  def dynamicSettings(settings: Settings, properties: String*): Settings = {
    val entries: IndexedSeq[YMapEntry] = map.entries.filterNot { entry =>
      val key: String = entry.key.as[YScalar].text
      properties.contains(key) || isRamlAnnotation(key)
    }

    if (entries.nonEmpty) {
      val node = DataNodeParser(YNode(YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))),
                                parent = Some(settings.id))(WebApiShapeParserContextAdapter(ctx)).parse()
      settings.set(SettingsModel.AdditionalProperties, node)
    }
    settings
  }

  private def apiKey() = {
    val settings = scheme.withApiKeySettings()
    map.key("name", ApiKeySettingsModel.Name in settings)
    map.key("in", ApiKeySettingsModel.In in settings)
    dynamicSettings(settings, "name", "in")
  }

  protected def oauth2(): OAuth2Settings = {
    val settings = scheme.withOAuth2Settings()
    val flow     = new Lazy[OAuth2Flow](() => OAuth2Flow(map).adopted(settings.id))

    map.key("authorizationUri", entry => (OAuth2FlowModel.AuthorizationUri in flow.getOrCreate).apply(entry))
    map.key("accessTokenUri",
            entry => (OAuth2FlowModel.AccessTokenUri in flow.getOrCreate).allowingAnnotations.apply(entry))
    map.key("flow".asRamlAnnotation, entry => (OAuth2FlowModel.Flow in flow.getOrCreate).apply(entry))
    map.key("authorizationGrants", (OAuth2SettingsModel.AuthorizationGrants in settings).allowingSingleValue)

    val ScopeParser = (n: YNode) => {
      val element = ScalarNode(n).text()
      scheme match {
        case ss: ParametrizedSecurityScheme =>
          val effectiveScheme =
            if (ss.scheme.isLink) ss.scheme.effectiveLinkTarget().asInstanceOf[SecurityScheme] else ss.scheme
          effectiveScheme.settings match {
            case se: OAuth2Settings if isValidScope(se.flows.headOption, element.toString()) =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
            case _: OAuth2Settings =>
              val scope = Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
              ctx.eh.violation(
                UnknownScopeErrorSpecification,
                scope.id,
                s"Scope '${element.toString}' not found in settings of declared secured by ${ss.scheme.name.value()}.",
                n.location
              )
              scope
            case _ =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
          }
        case _ =>
          Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
      }
    }

    map.key("scopes").foreach { entry =>
      (OAuth2FlowModel.Scopes in flow.getOrCreate using ScopeParser).allowingSingleValue.apply(entry)
    }

    flow.option.foreach(f =>
      settings.fields
        .set(settings.id, OAuth2SettingsModel.Flows, AmfArray(Seq(f), Annotations.virtual()), Annotations.inferred()))

    dynamicSettings(settings, "authorizationUri", "accessTokenUri", "authorizationGrants", "scopes")
    settings
  }

  private def oauth1() = {
    val settings = scheme.withOAuth1Settings()

    map.key("requestTokenUri", (OAuth1SettingsModel.RequestTokenUri in settings).allowingAnnotations)
    map.key("authorizationUri", (OAuth1SettingsModel.AuthorizationUri in settings).allowingAnnotations)
    map.key("tokenCredentialsUri", (OAuth1SettingsModel.TokenCredentialsUri in settings).allowingAnnotations)
    map.key("signatures", OAuth1SettingsModel.Signatures in settings)

    dynamicSettings(settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
  }

  private def isValidScope(maybeFlow: Option[OAuth2Flow], scope: String): Boolean =
    maybeFlow.exists(flow => flow.scopes.isEmpty || flow.scopes.map(_.name.value()).contains(scope))

}

object RamlSecuritySettingsParser {
  def parse(scheme: SecurityScheme)(node: YNode)(implicit ctx: RamlWebApiContext): Settings = {
    ctx.factory.securitySettingsParser(node, scheme.`type`.value(), scheme).parse()
  }
}

class Raml10SecuritySettingsParser(node: YNode, `type`: String, scheme: DomainElement with WithSettings)(
    implicit override val ctx: RamlWebApiContext)
    extends RamlSecuritySettingsParser(node, `type`, scheme) {

  override protected def oauth2(): OAuth2Settings = {
    val settings = super.oauth2()
    validateRequiredFields(settings)
    settings
  }

  private def validateRequiredFields(settings: OAuth2Settings): Unit = {
    val grants            = settings.authorizationGrants.flatMap(_.option())
    val requiredFields    = requiredFieldsWithGrant(grants)
    val keys: Seq[String] = map.entries.map(ctx.getEntryKey)
    requiredFields.foreach {
      case (requiredField, grant) =>
        if (!keys.contains(requiredField.name))
          ctx.eh.warning(MissingRequiredFieldForGrantType,
                         settings.id,
                         s"'${requiredField.name}' is required when '$grant' grant type is used",
                         map.location)
    }
  }

  private val oauth2SpecFields = Map(
    "authorization_code" -> SpecNode(requiredFields = Set(SpecField("accessTokenUri"), SpecField("authorizationUri"))),
    "password"           -> SpecNode(requiredFields = Set(SpecField("accessTokenUri"))),
    "client_credentials" -> SpecNode(requiredFields = Set(SpecField("accessTokenUri"))),
    "implicit"           -> SpecNode(requiredFields = Set(SpecField("authorizationUri")))
  )

  private def requiredFieldsWithGrant(grants: Seq[String]): mutable.Map[SpecField, String] = {
    val requiredFields = mutable.Map.empty[SpecField, String]
    grants.foreach { grantType =>
      oauth2SpecFields.get(grantType).map(_.requiredFields).map { fieldsForGrant =>
        fieldsForGrant.foreach { requiredField =>
          requiredFields += requiredField -> grantType
        }
      }
    }
    requiredFields
  }
}
