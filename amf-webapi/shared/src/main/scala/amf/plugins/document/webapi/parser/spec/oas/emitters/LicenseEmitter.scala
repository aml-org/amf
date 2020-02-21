package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.domain.webapi.metamodel.LicenseModel
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

case class LicenseEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value,
      b.entry(
        key,
        _.obj { b =>
          val fs     = f.obj.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))
          fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

          result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

          traverse(ordering.sorted(result), b)
        }
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
