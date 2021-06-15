package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Fields
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, ValueEmitter, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.contexts.SpecEmitterContext
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.models.{License, Organization}
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

case class InfoEmitter(fs: Fields, ordering: SpecOrdering)(implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(WebApiModel.Name)
      .fold(result += MapEntryEmitter("title", "API"))(f => result += ValueEmitter("title", f))

    fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

    fs.entry(WebApiModel.Version)
      .fold(result += MapEntryEmitter("version", "1.0"))(f => result += ValueEmitter("version", f))

    fs.entry(WebApiModel.License)
      .map(f => result += LicenseEmitter("license", f.value.value.asInstanceOf[License], ordering))

    fs.entry(WebApiModel.Provider)
      .map(f => result += OrganizationEmitter("contact", f.value.value.asInstanceOf[Organization], ordering))

    b.entry(
      "info",
      _.obj(traverse(ordering.sorted(result), _))
    )
  }

  override def position(): Position = {
    var result: Position = ZERO
    fs.entry(WebApiModel.Version)
      .foreach(
        f =>
          f.value.annotations
            .find(classOf[LexicalInformation])
            .foreach({
              case LexicalInformation(range) => result = range.start
            }))
    fs.entry(WebApiModel.Name)
      .foreach(
        f =>
          f.value.annotations
            .find(classOf[LexicalInformation])
            .foreach({
              case LexicalInformation(range) =>
                if (result.isZero || range.start.lt(result)) {
                  result = range.start
                }
            }))
    result
  }
}
