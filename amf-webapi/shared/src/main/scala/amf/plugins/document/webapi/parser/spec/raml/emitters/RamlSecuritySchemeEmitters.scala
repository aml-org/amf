package amf.plugins.document.webapi.parser.spec.raml.emitters

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DataNode
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.ReferenceEmitterHelper.emitLinkOr
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{AnnotationsEmitter, DataNodeEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml10TypePartEmitter, RamlNamedTypeEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  ApiShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlSecuritySchemesEmitters(
    securitySchemes: Seq[SecurityScheme],
    references: Seq[BaseUnit],
    ordering: SpecOrdering,
    namedSecurityEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlNamedSecuritySchemeEmitter)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "securitySchemes",
      _.obj(traverse(ordering.sorted(securitySchemes.map(s => namedSecurityEmitter(s, references, ordering))), _)))

  }

  override def position(): Position =
    securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class Raml10NamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                            references: Seq[BaseUnit],
                                            ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlNamedSecuritySchemeEmitter(securityScheme, references, ordering) {
  override protected def securitySchemeEmitter
    : (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlSecuritySchemeEmitter = Raml10SecuritySchemeEmitter.apply
}

case class Raml08NamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                            references: Seq[BaseUnit],
                                            ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlNamedSecuritySchemeEmitter(securityScheme, references, ordering) {
  override protected def securitySchemeEmitter
    : (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlSecuritySchemeEmitter = Raml08SecuritySchemeEmitter.apply
}

abstract class RamlNamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                              references: Seq[BaseUnit],
                                              ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  protected def securitySchemeEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlSecuritySchemeEmitter

  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = securityScheme.name
      .option()
      .getOrElse(throw new Exception(s"Cannot declare security scheme without name $securityScheme"))

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    RamlTagToReferenceEmitter(securityScheme, references).emit(b)
  }

  private def emitInline(b: PartBuilder): Unit =
    b.obj(traverse(ordering.sorted(securitySchemeEmitter(securityScheme, references, ordering).emitters()), _))

}

case class Raml10SecuritySchemeEmitter(securityScheme: SecurityScheme,
                                       references: Seq[BaseUnit],
                                       ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlSecuritySchemeEmitter(securityScheme, references, ordering) {
  override protected def describedByEmitter
    : (String, SecurityScheme, SpecOrdering, Seq[BaseUnit]) => DescribedByEmitter = Raml10DescribedByEmitter.apply
}

case class Raml08SecuritySchemeEmitter(securityScheme: SecurityScheme,
                                       references: Seq[BaseUnit],
                                       ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlSecuritySchemeEmitter(securityScheme, references, ordering) {
  override protected def describedByEmitter
    : (String, SecurityScheme, SpecOrdering, Seq[BaseUnit]) => DescribedByEmitter = Raml10DescribedByEmitter.apply
}

abstract class RamlSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                         references: Seq[BaseUnit],
                                         ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  override def emit(b: PartBuilder): Unit = {
    emitLinkOr(securityScheme, b, references) {
      b.obj(traverse(ordering.sorted(emitters()), _))
    }
  }

  override def position(): Position = pos(securityScheme.annotations)

  protected def describedByEmitter: (String, SecurityScheme, SpecOrdering, Seq[BaseUnit]) => DescribedByEmitter

  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val fs      = securityScheme.fields

    emitType(results, securityScheme)
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += RamlScalarEmitter("displayName", f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += RamlScalarEmitter("description", f))

    results += describedByEmitter("describedBy", securityScheme, ordering, references)

    if (!isHttpBasicAuth(securityScheme) && !isHttpDigestAuth(securityScheme))
      fs.entry(SecuritySchemeModel.Settings).map(f => results += RamlSecuritySettingsEmitter(f, ordering))

    results

  }

  private def emitType(results: ListBuffer[EntryEmitter], securityScheme: SecurityScheme): Unit =
    securityScheme.fields.entry(SecuritySchemeModel.Type) foreach {
      case f if f.scalar.toString == "Api Key" =>
        results += MapEntryEmitter("type", "x-apiKey", position = pos(f.value.annotations))
      case f if f.scalar.toString == "openIdConnect" =>
        results += MapEntryEmitter("type", "x-openIdConnect", position = pos(f.value.annotations))
      case f if isHttpBasicAuth(securityScheme) || isHttpDigestAuth(securityScheme) =>
        results ++= emitSupportedHttpAuthTypes(f, securityScheme.settings)
      case f => results += RamlScalarEmitter("type", f)
    }

  private def emitSupportedHttpAuthTypes(typeField: FieldEntry, settings: Settings): ListBuffer[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val resultantType =
      settings.fields.entry(HttpSettingsModel.Scheme).map(x => x.scalar.toString).getOrElse("") match {
        case "basic"  => "Basic Authentication"
        case "digest" => "Digest Authentication"
        case _        => ""
      }
    results += MapEntryEmitter("type", resultantType, position = pos(typeField.value.annotations))
  }

  private def isHttpAuth(securityScheme: SecurityScheme): Boolean = securityScheme.`type`.value() == "http"
  private def hasHttpAuthScheme(scheme: String, securityScheme: SecurityScheme): Boolean =
    securityScheme.settings.fields.entry(HttpSettingsModel.Scheme).exists(s => s.scalar.toString == scheme)
  private def isHttpBasicAuth(securityScheme: SecurityScheme): Boolean =
    isHttpAuth(securityScheme) && hasHttpAuthScheme("basic", securityScheme)
  private def isHttpDigestAuth(securityScheme: SecurityScheme): Boolean =
    isHttpAuth(securityScheme) && hasHttpAuthScheme("digest", securityScheme)
}

case class RamlSecuritySettingsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry("settings", _.obj(traverse(ordering.sorted(RamlSecuritySettingsValuesEmitters(f, ordering).emitters), _)))
  }
  override def position(): Position = pos(f.value.annotations)
}

case class RamlSecuritySettingsValuesEmitters(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = {
    val settings = f.value.value.asInstanceOf[Settings]
    val results  = ListBuffer[EntryEmitter]()

    results ++= (settings match {
      case o1: OAuth1Settings     => RamlOAuth1SettingsEmitters(o1, ordering).emitters()
      case o2: OAuth2Settings     => RamlOAuth2SettingsEmitters(o2, ordering).emitters()
      case apiKey: ApiKeySettings => RamlApiKeySettingsEmitters(apiKey, ordering).emitters()
      case _                      => Nil
    })

    settings.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => results ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters())
    results
  }
}

case class RamlApiKeySettingsEmitters(apiKey: ApiKeySettings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val fs      = apiKey.fields
    val results = ListBuffer[EntryEmitter]()

    fs.entry(ApiKeySettingsModel.Name).map(f => results += ValueEmitter("name", f))

    fs.entry(ApiKeySettingsModel.In).map(f => results += ValueEmitter("in", f))
    results
  }
}

case class RamlOAuth1SettingsEmitters(o1: OAuth1Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {
    val fs      = o1.fields
    val results = ListBuffer[EntryEmitter]()
    fs.entry(OAuth1SettingsModel.RequestTokenUri).map(f => results += RamlScalarEmitter("requestTokenUri", f))
    fs.entry(OAuth1SettingsModel.AuthorizationUri).map(f => results += RamlScalarEmitter("authorizationUri", f))
    fs.entry(OAuth1SettingsModel.TokenCredentialsUri).map(f => results += RamlScalarEmitter("tokenCredentialsUri", f))
    fs.entry(OAuth1SettingsModel.Signatures).map(f => results += spec.arrayEmitter("signatures", f, ordering))
    results
  }
}

case class RamlOAuth2SettingsEmitters(o2: OAuth2Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {

  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  def emitters(): Seq[EntryEmitter] = {

    val fs      = o2.fields
    val results = ListBuffer[EntryEmitter]()

    o2.flows.headOption.foreach(flowEmitters(_, results))

    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => results += spec.arrayEmitter("authorizationGrants", f, ordering))

    results
  }

  def flowEmitters(flow: OAuth2Flow, results: ListBuffer[EntryEmitter]): Unit = {
    val fs = flow.fields

    fs.entry(OAuth2FlowModel.AuthorizationUri).map(f => results += ValueEmitter("authorizationUri", f))
    fs.entry(OAuth2FlowModel.AccessTokenUri).map(f => results += RamlScalarEmitter("accessTokenUri", f))
    fs.entry(OAuth2FlowModel.Scopes).map(f => { results += RamlOAuth2ScopeEmitter("scopes", f, ordering) })
  }
}

case class RamlOAuth2ScopeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val namesEmitters = f.array.values.collect({ case s: Scope => RawEmitter(s.name.value()) })

    b.entry(key, _.list(traverse(ordering.sorted(namesEmitters), _)))

  } // todo : name and description?
  override def position(): Position = pos(f.value.annotations)
}

case class Raml10DescribedByEmitter(key: String,
                                    securityScheme: SecurityScheme,
                                    ordering: SpecOrdering,
                                    references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends DescribedByEmitter(key, securityScheme, ordering, references) {

  override def entries(fs: Fields): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    fs.entry(SecuritySchemeModel.QueryString)
      .foreach { f =>
        f.value.value match {
          case shape: AnyShape =>
            results += RamlNamedTypeEmitter(shape, ordering, references, Raml10TypePartEmitter.apply)
          case _ => // ignore
        }

      }

    results ++= AnnotationsEmitter(securityScheme, ordering).emitters

    super.entries(fs) ++ results
  }
}

case class Raml08DescribedByEmitter(key: String,
                                    securityScheme: SecurityScheme,
                                    ordering: SpecOrdering,
                                    references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends DescribedByEmitter(key, securityScheme, ordering, references) {

  override def entries(fs: Fields): Seq[EntryEmitter] = {
    fs.entry(SecuritySchemeModel.QueryString)
      .foreach { _ =>
        spec.eh.violation(ResolutionValidation,
                          securityScheme.id,
                          None,
                          "Cannot emit query string in raml 08 spec",
                          securityScheme.position(),
                          securityScheme.location())
      }

    super.entries(fs)
  }
}

abstract class DescribedByEmitter(key: String,
                                  securityScheme: SecurityScheme,
                                  ordering: SpecOrdering,
                                  references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  def entries(fs: Fields): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    fs.entry(SecuritySchemeModel.Headers)
      .foreach(f => results += RamlParametersEmitter("headers", f, ordering, references))
    fs.entry(SecuritySchemeModel.QueryParameters)
      .foreach { f =>
        if (f.array.values.nonEmpty)
          results += RamlParametersEmitter("queryParameters", f, ordering, references)
      }
    fs.entry(SecuritySchemeModel.Responses)
      .foreach(f => results += RamlResponsesEmitter("responses", f, ordering, references))

    results
  }

  def emit(b: EntryBuilder): Unit = {
    val fs      = securityScheme.fields
    val results = entries(fs)

    if (results.nonEmpty)
      b.entry(key, _.obj(traverse(ordering.sorted(results), _)))

  }

  override def position(): Position =
    (securityScheme.headers ++ securityScheme.queryParameters ++ securityScheme.responses).headOption
      .map(h => pos(h.annotations))
      .getOrElse(Position.ZERO)
}
