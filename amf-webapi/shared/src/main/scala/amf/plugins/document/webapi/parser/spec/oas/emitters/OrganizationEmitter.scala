package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

case class OrganizationEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(
    implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value,
      b.entry(
        key,
        _.obj { b =>
          val fs     = f.obj.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))
          fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))
          fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

          result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

          traverse(ordering.sorted(result), b)
        }
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
