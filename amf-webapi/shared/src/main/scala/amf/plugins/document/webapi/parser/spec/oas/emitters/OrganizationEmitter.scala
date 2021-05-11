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
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import amf.plugins.domain.webapi.models.Organization
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class OrganizationEmitter(key: String, org: Organization, ordering: SpecOrdering)(
    implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      org.annotations,
      b.entry(
        key,
        OrganizationPartEmitter(org, ordering).emit(_)
      )
    )
  }

  override def position(): Position = pos(org.annotations)
}

case class OrganizationPartEmitter(org: Organization, ordering: SpecOrdering)(implicit val spec: SpecEmitterContext)
    extends PartEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: PartBuilder): Unit = {
    b.obj { b =>
      val fs     = org.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))
      fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))
      fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

      result ++= AnnotationsEmitter(org, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  override def position(): Position = pos(org.annotations)
}
