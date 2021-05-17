package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.models.License
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class LicenseEmitter(key: String, license: License, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      license.annotations,
      b.entry(
        key,
        LicensePartEmitter(license, ordering).emit(_)
      )
    )
  }

  override def position(): Position = pos(license.annotations)
}

case class LicensePartEmitter(license: License, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: PartBuilder): Unit = {
    b.obj { b =>
      val fs     = license.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))
      fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

      result ++= AnnotationsEmitter(license, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  override def position(): Position = pos(license.annotations)
}
