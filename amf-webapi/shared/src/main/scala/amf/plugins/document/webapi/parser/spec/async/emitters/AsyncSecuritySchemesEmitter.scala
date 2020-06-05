package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.Oas3OAuth2SettingsEmitters
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncSecuritySchemesEmitter(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val emitters = ordering.sorted(securitySchemes.map(AsyncSingleSchemeEmitter(_, ordering)))
    b.entry(
      YNode("securitySchemes"),
      _.obj(ob => traverse(emitters, ob))
    )
  }

  override def position(): Position = securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

private case class AsyncSingleSchemeEmitter(scheme: SecurityScheme, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      scheme.name.value(),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = scheme.fields
        fs.entry(SecuritySchemeModel.Type)
          .foreach(f => result += MapEntryEmitter("type", scheme.name.value(), position = pos(f.value.annotations)))
        fs.entry(SecuritySchemeModel.Description).foreach(f => result += ValueEmitter("description", f))
        fs.entry(SecuritySchemeModel.Settings)
          .foreach(_ => result ++= new AsyncSecuritySettingsEmitter(scheme.settings, ordering).emitters)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(scheme.annotations)
}

class AsyncSecuritySettingsEmitter(settings: Settings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {

  def emitters: Seq[EntryEmitter] = emittersFor(settings)

  private def emittersFor(settings: Settings) = {
    val particularEmitters = settings match {
      case settings: HttpSettings          => HttpSettingsEmitters(settings, ordering).emitters()
      case settings: ApiKeySettings        => ApiKeySettingsEmitters(settings, ordering).emitters()
      case settings: HttpApiKeySettings    => HttpApiKeySettingsEmitters(settings, ordering).emitters()
      case settings: OpenIdConnectSettings => OpenIdConnectSettingsEmitters(settings, ordering).emitters()
      case settings: OAuth2Settings        => Oas3OAuth2SettingsEmitters(settings, ordering).emitters()
      case _                               => Seq()
    }
    val result = particularEmitters ++ AnnotationsEmitter(settings, ordering).emitters
    ordering.sorted(result)
  }
}

case class HttpApiKeySettingsEmitters(settings: HttpApiKeySettings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs     = settings.fields
    val result = ListBuffer[EntryEmitter]()

    fs.entry(HttpApiKeySettingsModel.Name).map(f => result += ValueEmitter("name", f))
    fs.entry(HttpApiKeySettingsModel.In).map(f => result += ValueEmitter("in", f))
    result
  }
}

case class HttpSettingsEmitters(settings: HttpSettings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs     = settings.fields
    val result = ListBuffer[EntryEmitter]()

    fs.entry(HttpSettingsModel.Scheme).map(f => result += ValueEmitter("scheme", f))
    fs.entry(HttpSettingsModel.BearerFormat).map(f => result += ValueEmitter("bearerFormat", f))
    result
  }
}

case class OpenIdConnectSettingsEmitters(settings: OpenIdConnectSettings, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs     = settings.fields
    val result = ListBuffer[EntryEmitter]()

    fs.entry(OpenIdConnectSettingsModel.Url).map(f => result += ValueEmitter("openIdConnectUrl", f))
    result
  }
}

case class ApiKeySettingsEmitters(settings: ApiKeySettings, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs     = settings.fields
    val result = ListBuffer[EntryEmitter]()

    fs.entry(ApiKeySettingsModel.In).map(f => result += ValueEmitter("in", f))
    result
  }
}
