package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.spec.common.emitter.{AgnosticShapeEmitterContextAdapter, SpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.domain.{RamlApiKeySettingsEmitters, RamlOAuth1SettingsEmitters}
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.DataNode
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.oas.emitter.OasOrphanAnnotationsEmitter
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.YNode

import scala.collection.mutable.ListBuffer

case class OasSecuritySettingsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val settings = f.value.value.asInstanceOf[Settings]

    settings match {
      case o1: OAuth1Settings                            => OasOAuth1SettingsEmitters(o1, ordering).emitters()
      case o2: OAuth2Settings if spec.spec == Spec.OAS30 => Oas3OAuth2SettingsEmitters(o2, ordering).emitters()
      case o2: OAuth2Settings                            => OasOAuth2SettingsEmitters(o2, ordering).emitters()
      case apiKey: ApiKeySettings                        => OasApiKeySettingsEmitters(apiKey, ordering).emitters()
      case http: HttpSettings                            => OasHttpSettingsEmitters(http, ordering).emitters()
      case openId: OpenIdConnectSettings => OasOpenIdConnectSettingsEmitters(openId, ordering).emitters()
      case _ =>
        val internals = ListBuffer[EntryEmitter]()
        settings.fields
          .entry(SettingsModel.AdditionalProperties)
          .foreach(f =>
            internals ++= DataNodeEmitter(f.value.value.asInstanceOf[DataNode], ordering)(spec.eh).emitters()
          )
        if (internals.nonEmpty)
          Seq(OasSettingsTypeEmitter(internals, settings, ordering))
        else Nil
    }
  }
}

case class OasOpenIdConnectSettingsEmitters(settings: Settings, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    fs.entry(OpenIdConnectSettingsModel.Url).map(f => externals += ValueEmitter("openIdConnectUrl", f))

    externals ++= AnnotationsEmitter(settings, ordering).emitters
  }
}

case class OasHttpSettingsEmitters(settings: Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    fs.entry(HttpSettingsModel.Scheme).map(f => externals += ValueEmitter("scheme", f))

    fs.entry(HttpSettingsModel.BearerFormat).map(f => externals += ValueEmitter("bearerFormat", f))

    externals ++= AnnotationsEmitter(settings, ordering).emitters
  }
}

case class OasApiKeySettingsEmitters(apiKey: ApiKeySettings, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) {
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

case class OasOAuth2SettingsEmitters(settings: OAuth2Settings, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {
    val fs        = settings.fields
    val externals = ListBuffer[EntryEmitter]()

    settings.flows.headOption.foreach(flowEmitters(_, externals))

    val internals = ListBuffer[EntryEmitter]()
    fs.entry(OAuth2SettingsModel.AuthorizationGrants)
      .map(f => internals += spec.arrayEmitter("authorizationGrants", f, ordering))

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

case class Oas3OAuth2SettingsEmitters(settings: OAuth2Settings, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) {

  def emitters(): Seq[EntryEmitter] = {
    val externals = ListBuffer[EntryEmitter]()

    externals += Oas3OAuth2FlowEmitter(settings, ordering)

    externals
  }
}

private case class Oas3OAuth2FlowEmitter(settings: OAuth2Settings, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: EntryBuilder): Unit = {
    val fs                               = settings.fields
    val result: ListBuffer[EntryEmitter] = ListBuffer()

    val orphanAnnotations =
      settings.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

    fs.entry(OAuth2SettingsModel.Flows)
      .foreach(f => result += Oas3OAuthFlowsEmitter(f, ordering, orphanAnnotations))

    result ++= AnnotationsEmitter(settings, ordering).emitters

    traverse(ordering.sorted(result), b)
  }

  override def position(): Position =
    settings.flows.headOption.map(flow => pos(flow.annotations)).getOrElse(Position.ZERO)
}

private case class Oas3OAuthFlowsEmitter(
    f: FieldEntry,
    ordering: SpecOrdering,
    orphanAnnotations: Seq[DomainExtension]
)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {

    val flows: Seq[OAuth2Flow] = f.arrayValues

    val emitters = flows.map(flow => Oas3OAuthFlowEmitter(flow, ordering)) ++ flowsElementAnnotations()
    b.entry("flows", _.obj(traverse(ordering.sorted(emitters), _)))
  }

  private def flowsElementAnnotations(): Seq[EntryEmitter] =
    OasOrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters

  override def position(): Position = pos(f.value.annotations)
}

private case class Oas3OAuthFlowEmitter(flow: OAuth2Flow, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {
    val fs = flow.fields

    b.entry(
      YNode(flow.flow.value()),
      builder => {
        builder.obj { mapBuilder =>
          val builders = ListBuffer[EntryEmitter]()

          fs.entry(OAuth2FlowModel.AuthorizationUri)
            .map(f => builders += ValueEmitter("authorizationUrl", f))

          fs.entry(OAuth2FlowModel.AccessTokenUri).map(f => builders += ValueEmitter("tokenUrl", f))

          fs.entry(OAuth2FlowModel.RefreshUri).map(f => builders += ValueEmitter("refreshUrl", f))

          val orphanAnnotations =
            flow.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

          fs.entry(OAuth2FlowModel.Scopes)
            .foreach(f => builders += OasOAuth2ScopeEmitter("scopes", f, ordering, orphanAnnotations))

          builders ++= AnnotationsEmitter(flow, ordering).emitters
          traverse(ordering.sorted(builders), mapBuilder)
        }
      }
    )
  }

  override def position(): Position = pos(flow.annotations)
}

case class OasOAuth2ScopeEmitter(
    key: String,
    f: FieldEntry,
    ordering: SpecOrdering,
    orphanAnnotations: Seq[DomainExtension]
)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: EntryBuilder): Unit = {
    val emitters = OasScopeValuesEmitters(f).emitters() ++ scopesElementAnnotations()
    b.entry(key, _.obj(traverse(ordering.sorted(emitters), _)))
  } // todo : name and description?

  private def scopesElementAnnotations(): Seq[EntryEmitter] = {
    OasOrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
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
    sourceOr(
      settings.annotations,
      b.entry("settings".asOasExtension, _.obj(traverse(ordering.sorted(settingsEntries), _)))
    )
  }

  override def position(): Position = settingsEntries.headOption.map(_.position()).getOrElse(Position.ZERO)
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
