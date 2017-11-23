package amf.spec.declaration

import amf.framework.model.document.BaseUnit
import amf.domain.FieldEntry
import amf.domain.extensions.DataNode
import amf.domain.security._
import amf.metadata.domain.security._
import amf.parser.Position
import amf.shape.Shape
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.domain.{RamlParametersEmitter, RamlResponsesEmitter}
import amf.spec.oas.{OasSecuritySchemeType, OasSecuritySchemeTypeMapping}
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasSecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val securityTypes: Map[OasSecuritySchemeType, SecurityScheme] =
      securitySchemes.map(s => OasSecuritySchemeTypeMapping.fromText(s.`type`) -> s).toMap
    val (oasSecurityDefinitions, extensionDefinitions) = securityTypes.partition(m => m._1.isOas)

    if (oasSecurityDefinitions.nonEmpty)
      b.entry("securityDefinitions",
              _.obj(
                traverse(ordering.sorted(
                           oasSecurityDefinitions.map(s => OasNamedSecuritySchemeEmitter(s._2, s._1, ordering)).toSeq),
                         _)))
    if (extensionDefinitions.nonEmpty)
      b.entry(
        "x-securitySchemes",
        _.obj(
          traverse(
            ordering.sorted(extensionDefinitions.map(s => OasNamedSecuritySchemeEmitter(s._2, s._1, ordering)).toSeq),
            _)))

  }

  override def position(): Position =
    securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class RamlSecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme],
                                       references: Seq[BaseUnit],
                                       ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "securitySchemes",
      _.obj(
        traverse(ordering.sorted(securitySchemes.map(s => RamlNamedSecuritySchemeEmitter(s, references, ordering))),
                 _)))

  }

  override def position(): Position =
    securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class OasNamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                         mapType: OasSecuritySchemeType,
                                         ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = Option(securityScheme.name)
      .getOrElse(throw new Exception(s"Cannot declare security scheme without name $securityScheme"))

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    securityScheme.linkTarget.foreach { l =>
      OasTagToReferenceEmitter(l, securityScheme.linkLabel).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit =
    b.obj(traverse(ordering.sorted(OasSecuritySchemeEmitter(securityScheme, mapType, ordering).emitters()), _))

}

case class RamlNamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                          references: Seq[BaseUnit],
                                          ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = Option(securityScheme.name)
      .getOrElse(throw new Exception(s"Cannot declare security scheme without name $securityScheme"))

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    securityScheme.linkTarget.foreach { l =>
      RamlTagToReferenceEmitter(l, securityScheme.linkLabel.getOrElse(l.id), references).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit =
    b.obj(traverse(ordering.sorted(RamlSecuritySchemeEmitter(securityScheme, references, ordering).emitters()), _))

}

case class OasSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                    mapType: OasSecuritySchemeType,
                                    ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val results = ListBuffer[EntryEmitter]()

    val fs = securityScheme.fields

    fs.entry(SecuritySchemeModel.Type)
      .map(f => {
        results += MapEntryEmitter("type", mapType.text, position = pos(f.value.annotations))

      }) // todo x-apiKey type??
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += ValueEmitter("x-displayName", f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += ValueEmitter("description", f))

    results += DescribedByEmitter("x-describedBy", securityScheme, ordering, Nil)

    fs.entry(SecuritySchemeModel.Settings).map(f => results ++= OasSecuritySettingsEmitter(f, ordering).emitters())

    ordering.sorted(results)

  }
}

case class RamlSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                     references: Seq[BaseUnit],
                                     ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val fs      = securityScheme.fields

    fs.entry(SecuritySchemeModel.Type).map(f => results += ValueEmitter("type", f))
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += ValueEmitter("displayName", f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += ValueEmitter("description", f))

    results += DescribedByEmitter("describedBy", securityScheme, ordering, references)

    fs.entry(SecuritySchemeModel.Settings).map(f => results += RamlSecuritySettingsEmitter(f, ordering))

    results

  }
}

case class RamlSecuritySettingsEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry("settings", _.obj(traverse(ordering.sorted(RamlSecuritySettingsValuesEmitters(f, ordering).emitters), _)))
  }
  override def position(): Position = pos(f.value.annotations)
}

case class RamlSecuritySettingsValuesEmitters(f: FieldEntry, ordering: SpecOrdering) {
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
      .foreach(f => results ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering).emitters())
    results
  }
}

case class OasSecuritySettingsEmitter(f: FieldEntry, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {

    val settings = f.value.value.asInstanceOf[Settings]

    settings match {
      case o1: OAuth1Settings     => OasOAuth1SettingsEmitters(o1, ordering).emitters()
      case o2: OAuth2Settings     => OasOAuth2SettingsEmitters(o2, ordering).emitters()
      case apiKey: ApiKeySettings => OasApiKeySettingsEmitters(apiKey, ordering).emitters()
      case _ =>
        val internals = ListBuffer[EntryEmitter]()
        settings.fields
          .entry(SettingsModel.AdditionalProperties)
          .foreach(f => internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering).emitters())
        if (internals.nonEmpty)
          Seq(OasSettingsTypeEmitter(internals, settings, ordering))
        else Nil
    }
  }
}

case class OasApiKeySettingsEmitters(apiKey: ApiKeySettings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val fs      = apiKey.fields
    val results = ListBuffer[EntryEmitter]() ++= RamlApiKeySettingsEmitters(apiKey, ordering).emitters()

    val internals = ListBuffer[EntryEmitter]()
    apiKey.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering).emitters())

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

case class OasOAuth1SettingsEmitters(o1: OAuth1Settings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val fs      = o1.fields
    val results = ListBuffer[EntryEmitter]() ++= RamlOAuth1SettingsEmitters(o1, ordering).emitters()

    o1.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => results ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering).emitters())

    Seq(OasSettingsTypeEmitter(results, o1, ordering))
  }

}

case class RamlOAuth1SettingsEmitters(o1: OAuth1Settings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val fs      = o1.fields
    val results = ListBuffer[EntryEmitter]()

    fs.entry(OAuth1SettingsModel.RequestTokenUri).map(f => results += ValueEmitter("requestTokenUri", f))

    fs.entry(OAuth1SettingsModel.AuthorizationUri).map(f => results += ValueEmitter("authorizationUri", f))

    fs.entry(OAuth1SettingsModel.TokenCredentialsUri).map(f => results += ValueEmitter("tokenCredentialsUri", f))

    fs.entry(OAuth1SettingsModel.Signatures).map(f => results += ArrayEmitter("signatures", f, ordering))
    results
  }
}

case class OasOAuth2SettingsEmitters(settings: Settings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    fs.entry(OAuth2SettingsModel.AuthorizationUri).map(f => externals += ValueEmitter("authorizationUrl", f))

    fs.entry(OAuth2SettingsModel.AccessTokenUri).map(f => externals += ValueEmitter("tokenUrl", f))

    fs.entry(OAuth2SettingsModel.Flow).map(f => externals += ValueEmitter("flow", f))

    fs.entry(OAuth2SettingsModel.Scopes)
      .foreach(f => externals += OasOAuth2ScopeEmitter("scopes", f, ordering))

    val internals = ListBuffer[EntryEmitter]()
    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => internals += ArrayEmitter("authorizationGrants", f, ordering))

    settings.fields
      .entry(SettingsModel.AdditionalProperties)
      .foreach(f => internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering).emitters())

    if (internals.nonEmpty)
      externals += OasSettingsTypeEmitter(internals, settings, ordering)

    externals
  }

}

case class RamlOAuth2SettingsEmitters(o2: OAuth2Settings, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {

    val fs      = o2.fields
    val results = ListBuffer[EntryEmitter]()

    fs.entry(OAuth2SettingsModel.AuthorizationUri).map(f => results += ValueEmitter("authorizationUri", f))

    fs.entry(OAuth2SettingsModel.AccessTokenUri).map(f => results += ValueEmitter("accessTokenUri", f))

    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => results += ArrayEmitter("authorizationGrants", f, ordering))

    fs.entry(OAuth2SettingsModel.Scopes).map(f => { results += RamlOAuth2ScopeEmitter("scopes", f, ordering) })

    results
  }
}

case class RamlOAuth2ScopeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val namesEmitters = f.array.values.collect({ case s: Scope => RawEmitter(s.name) })

    b.entry(key, _.list(traverse(ordering.sorted(namesEmitters), _)))

  } // todo : name and description?
  override def position(): Position = pos(f.value.annotations)
}

case class OasOAuth2ScopeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(key, _.obj(traverse(ordering.sorted(OasScopeValuesEmitters(f).emitters()), _)))
  } // todo : name and description?
  override def position(): Position = pos(f.value.annotations)
}

case class OasScopeValuesEmitters(f: FieldEntry) {
  def emitters(): Seq[EntryEmitter] =
    f.array.values.collect({ case s: Scope => MapEntryEmitter(s.name, s.description) })
}

case class OasSettingsTypeEmitter(settingsEntries: Seq[EntryEmitter], settings: Settings, ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(settings.annotations, b.entry("x-settings", _.obj(traverse(ordering.sorted(settingsEntries), _))))
  }

  override def position(): Position = settingsEntries.headOption.map(_.position()).getOrElse(Position.ZERO)
}

case class DescribedByEmitter(key: String,
                              securityScheme: SecurityScheme,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  def emit(b: EntryBuilder): Unit = {
    val fs      = securityScheme.fields
    val results = ListBuffer[EntryEmitter]()

    fs.entry(SecuritySchemeModel.Headers)
      .foreach(f => results += RamlParametersEmitter("headers", f, ordering, references))
    fs.entry(SecuritySchemeModel.QueryParameters)
      .foreach(f => results += RamlParametersEmitter("queryParameters", f, ordering, references))
    fs.entry(SecuritySchemeModel.Responses)
      .foreach(f => results += RamlResponsesEmitter("responses", f, ordering, references))
    fs.entry(SecuritySchemeModel.QueryString)
      .foreach(f => results += RamlNamedTypeEmitter(f.value.value.asInstanceOf[Shape], ordering, references))

    results ++= AnnotationsEmitter(securityScheme, ordering).emitters

    if (results.nonEmpty)
      b.entry(key, _.obj(traverse(ordering.sorted(results), _)))

  }

  override def position(): Position =
    (securityScheme.headers ++ securityScheme.queryParameters ++ securityScheme.responses).headOption
      .map(h => pos(h.annotations))
      .getOrElse(Position.ZERO)
}
