package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.contexts.SpecEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.domain.apicontract.metamodel.LicenseModel
import amf.plugins.domain.apicontract.models.License
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
