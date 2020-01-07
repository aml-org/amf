package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DataNode
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.core.remote.Vendor
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.{OasSecuritySchemeType, OasSecuritySchemeTypeMapping}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YNode

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasSecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val securityTypeMap: Seq[(OasSecuritySchemeType, SecurityScheme)] =
      securitySchemes.map(s => (OasSecuritySchemeTypeMapping.fromText(s.`type`.value()), s))

    val (oasSecurityDefinitions, extensionDefinitions) = securityTypeMap.partition(m => m._1.isOas)
    val isOas3                                         = spec.vendor == Vendor.OAS30
    if (oasSecurityDefinitions.nonEmpty)
      b.entry(
        if (isOas3) "securitySchemes" else "securityDefinitions",
        _.obj(
          traverse(
            ordering.sorted(oasSecurityDefinitions
              .map(s => OasNamedSecuritySchemeEmitter(s._2, s._1, ordering))),
            _
          ))
      )
    if (extensionDefinitions.nonEmpty)
      b.entry(
        "securitySchemes".asOasExtension,
        _.obj(
          traverse(
            ordering.sorted(extensionDefinitions.map(s => OasNamedSecuritySchemeEmitter(s._2, s._1, ordering)).toSeq),
            _))
      )

  }

  override def position(): Position =
    securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

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

case class OasNamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                         mapType: OasSecuritySchemeType,
                                         ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = securityScheme.name.option() match {
      case Some(n) => n
      case None =>
        spec.eh.violation(
          ResolutionValidation,
          securityScheme.id,
          None,
          s"Cannot declare security scheme without name $securityScheme",
          securityScheme.position(),
          securityScheme.location()
        )
        "default-"
    }

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    securityScheme.linkTarget.foreach { l =>
      OasTagToReferenceEmitter(l, securityScheme.linkLabel.option(), Nil).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit =
    b.obj(traverse(ordering.sorted(OasSecuritySchemeEmitter(securityScheme, mapType, ordering).emitters()), _))

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

  protected def securitySchemeEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlSecuritySchemeEmitter

  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = securityScheme.name
      .option()
      .getOrElse(throw new Exception(s"Cannot declare security scheme without name $securityScheme"))

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    securityScheme.linkTarget.foreach { l =>
      RamlTagToReferenceEmitter(l, securityScheme.linkLabel.option(), references).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit =
    b.obj(traverse(ordering.sorted(securitySchemeEmitter(securityScheme, references, ordering).emitters()), _))

}

case class OasSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                    mapType: OasSecuritySchemeType,
                                    ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val results = ListBuffer[EntryEmitter]()

    val fs = securityScheme.fields

    fs.entry(SecuritySchemeModel.Type)
      .map(f => {
        results += MapEntryEmitter("type", mapType.text, position = pos(f.value.annotations))

      })
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += ValueEmitter("displayName".asOasExtension, f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += ValueEmitter("description", f))

    results += Raml10DescribedByEmitter("describedBy".asOasExtension, securityScheme, ordering, Nil)(toRaml(spec))

    fs.entry(SecuritySchemeModel.Settings).map(f => results ++= OasSecuritySettingsEmitter(f, ordering).emitters())

    ordering.sorted(results)

  }
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
                                         ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {

  protected def describedByEmitter: (String, SecurityScheme, SpecOrdering, Seq[BaseUnit]) => DescribedByEmitter

  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val fs      = securityScheme.fields

    emitType(results, fs)
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += RamlScalarEmitter("displayName", f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += RamlScalarEmitter("description", f))

    results += describedByEmitter("describedBy", securityScheme, ordering, references)

    fs.entry(SecuritySchemeModel.Settings).map(f => results += RamlSecuritySettingsEmitter(f, ordering))

    results

  }
  private def emitType(results: ListBuffer[EntryEmitter], fs: Fields): Unit =
    fs.entry(SecuritySchemeModel.Type) foreach {
      case f if f.scalar.toString == "Api Key" =>
        results += MapEntryEmitter("type", "x-apiKey", position = pos(f.value.annotations))
      case f => results += RamlScalarEmitter("type", f)
    }
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

case class OasSecuritySettingsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val settings = f.value.value.asInstanceOf[Settings]

    settings match {
      case o1: OAuth1Settings                                => OasOAuth1SettingsEmitters(o1, ordering).emitters()
      case o2: OAuth2Settings if spec.vendor == Vendor.OAS30 => Oas3OAuth2SettingsEmitters(o2, ordering).emitters()
      case o2: OAuth2Settings                                => OasOAuth2SettingsEmitters(o2, ordering).emitters()
      case apiKey: ApiKeySettings                            => OasApiKeySettingsEmitters(apiKey, ordering).emitters()
      case http: HttpSettings                                => OasHttpSettingsEmitters(http, ordering).emitters()
      case openId: OpenIdConnectSettings                     => OasOpenIdConnectSettingsEmitters(openId, ordering).emitters()
      case _ =>
        val internals = ListBuffer[EntryEmitter]()
        settings.fields
          .entry(SettingsModel.AdditionalProperties)
          .foreach(f =>
            internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters())
        if (internals.nonEmpty)
          Seq(OasSettingsTypeEmitter(internals, settings, ordering))
        else Nil
    }
  }
}

case class OasOpenIdConnectSettingsEmitters(settings: Settings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    fs.entry(OpenIdConnectSettingsModel.Url).map(f => externals += ValueEmitter("openIdConnectUrl", f))

    externals ++= AnnotationsEmitter(settings, ordering).emitters
  }
}

case class OasHttpSettingsEmitters(settings: Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    fs.entry(HttpSettingsModel.Scheme).map(f => externals += ValueEmitter("scheme", f))

    fs.entry(HttpSettingsModel.BearerFormat).map(f => externals += ValueEmitter("bearerFormat", f))

    externals ++= AnnotationsEmitter(settings, ordering).emitters
  }
}

case class OasApiKeySettingsEmitters(apiKey: ApiKeySettings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]() ++= RamlApiKeySettingsEmitters(apiKey, ordering).emitters()

    val internals = ListBuffer[EntryEmitter]()
    apiKey.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters())

    if (internals.nonEmpty)
      results += OasSettingsTypeEmitter(internals, apiKey, ordering)

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

case class OasOAuth1SettingsEmitters(o1: OAuth1Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]() ++= RamlOAuth1SettingsEmitters(o1, ordering).emitters()

    o1.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => results ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters())

    Seq(OasSettingsTypeEmitter(results, o1, ordering))
  }

}

case class RamlOAuth1SettingsEmitters(o1: OAuth1Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs      = o1.fields
    val results = ListBuffer[EntryEmitter]()
    fs.entry(OAuth1SettingsModel.RequestTokenUri).map(f => results += RamlScalarEmitter("requestTokenUri", f))
    fs.entry(OAuth1SettingsModel.AuthorizationUri).map(f => results += RamlScalarEmitter("authorizationUri", f))
    fs.entry(OAuth1SettingsModel.TokenCredentialsUri).map(f => results += RamlScalarEmitter("tokenCredentialsUri", f))
    fs.entry(OAuth1SettingsModel.Signatures).map(f => results += ArrayEmitter("signatures", f, ordering))
    results
  }
}

case class OasOAuth2SettingsEmitters(settings: OAuth2Settings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    settings.flows.headOption.foreach(flowEmitters(_, externals))

    val internals = ListBuffer[EntryEmitter]()
    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => internals += ArrayEmitter("authorizationGrants", f, ordering))

    settings.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters())

    if (internals.nonEmpty)
      externals += OasSettingsTypeEmitter(internals, settings, ordering)

    externals ++= AnnotationsEmitter(settings, ordering).emitters

    externals
  }

  def flowEmitters(flow: OAuth2Flow, externals: ListBuffer[EntryEmitter]): Unit = {
    val fs = flow.fields

    fs.entry(OAuth2FlowModel.AuthorizationUri).map(f => externals += ValueEmitter("authorizationUrl", f))

    fs.entry(OAuth2FlowModel.AccessTokenUri).map(f => externals += ValueEmitter("tokenUrl", f))

    fs.entry(OAuth2FlowModel.Flow).map(f => externals += ValueEmitter("flow", f))

    // Annotations collected from the "scopes" element that has no direct representation in any model element
    // They will be passed to the EndpointsEmitter
    val orphanAnnotations =
      settings.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

    fs.entry(OAuth2FlowModel.Scopes)
      .foreach(f => externals += OasOAuth2ScopeEmitter("scopes", f, ordering, orphanAnnotations))
  }
}

case class Oas3OAuth2SettingsEmitters(settings: OAuth2Settings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {

  def emitters(): Seq[EntryEmitter] = {
    val externals = ListBuffer[EntryEmitter]()

    externals += Oas3OAuth2FlowEmitter(settings, ordering)

    externals
  }
}

private case class Oas3OAuth2FlowEmitter(settings: OAuth2Settings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val fs                               = settings.fields
    val result: ListBuffer[EntryEmitter] = ListBuffer()

    val orphanAnnotations =
      settings.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

    fs.entry(OAuth2SettingsModel.Flows).foreach(f => result += Oas3OAuthFlowsEmitter(f, ordering, orphanAnnotations))

    result ++= AnnotationsEmitter(settings, ordering).emitters

    traverse(ordering.sorted(result), b)
  }

  override def position(): Position =
    settings.flows.headOption.map(flow => pos(flow.annotations)).getOrElse(Position.ZERO)
}

private case class Oas3OAuthFlowsEmitter(f: FieldEntry,
                                         ordering: SpecOrdering,
                                         orphanAnnotations: Seq[DomainExtension])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {

    val flows: Seq[OAuth2Flow] = f.arrayValues

    val emitters = flows.map(flow => Oas3OAuthFlowEmitter(flow, ordering)) ++ flowsElementAnnotations()
    b.entry("flows", _.obj(traverse(ordering.sorted(emitters), _)))
  }

  private def flowsElementAnnotations(): Seq[EntryEmitter] =
    OrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters

  override def position(): Position = pos(f.value.annotations)
}

private case class Oas3OAuthFlowEmitter(flow: OAuth2Flow, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = flow.fields

    b.entry(
      YNode(flow.flow.value()),
      builder => {
        builder.obj {
          mapBuilder =>
            val builders = ListBuffer[EntryEmitter]()

            fs.entry(OAuth2FlowModel.AuthorizationUri)
              .map(f => builders += ValueEmitter("authorizationUrl", f))

            fs.entry(OAuth2FlowModel.AccessTokenUri).map(f => builders += ValueEmitter("tokenUrl", f))

            fs.entry(OAuth2FlowModel.RefreshUri).map(f => builders += ValueEmitter("refreshUrl", f))

            val orphanAnnotations =
              flow.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

            fs.entry(OAuth2FlowModel.Scopes)
              .foreach(f => builders += OasOAuth2ScopeEmitter("scopes", f, ordering, orphanAnnotations))

            traverse(ordering.sorted(builders), mapBuilder)
        }
      }
    )
  }

  override def position(): Position = pos(flow.annotations)
}

case class RamlOAuth2SettingsEmitters(o2: OAuth2Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val fs      = o2.fields
    val results = ListBuffer[EntryEmitter]()

    o2.flows.headOption.foreach(flowEmitters(_, results))

    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => results += ArrayEmitter("authorizationGrants", f, ordering))

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

case class OasOAuth2ScopeEmitter(key: String,
                                 f: FieldEntry,
                                 ordering: SpecOrdering,
                                 orphanAnnotations: Seq[DomainExtension])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val emitters = OasScopeValuesEmitters(f).emitters() ++ scopesElementAnnotations()
    b.entry(key, _.obj(traverse(ordering.sorted(emitters), _)))
  } // todo : name and description?

  private def scopesElementAnnotations(): Seq[EntryEmitter] = {
    OrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasScopeValuesEmitters(f: FieldEntry) {
  def emitters(): Seq[EntryEmitter] =
    f.array.values.collect({ case s: Scope => MapEntryEmitter(s.name.value(), s.description.value()) })
}

case class OasSettingsTypeEmitter(settingsEntries: Seq[EntryEmitter], settings: Settings, ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(settings.annotations,
             b.entry("settings".asOasExtension, _.obj(traverse(ordering.sorted(settingsEntries), _))))
  }

  override def position(): Position = settingsEntries.headOption.map(_.position()).getOrElse(Position.ZERO)
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
