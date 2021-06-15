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
import amf.plugins.domain.apicontract.metamodel.OrganizationModel
import amf.plugins.domain.apicontract.models.Organization
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
