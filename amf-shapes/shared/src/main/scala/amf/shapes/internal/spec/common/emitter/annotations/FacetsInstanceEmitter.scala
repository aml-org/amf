package amf.shapes.internal.spec.common.emitter.annotations

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.ShapeExtension
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.spec.common.emitter.{DataNodeEmitter, ShapeEmitterContext}
import org.yaml.model.YDocument.EntryBuilder

case class FacetsEmitter(element: Shape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] = element.customShapeProperties.map { extension: ShapeExtension =>
    spec.facetsInstanceEmitter(extension, ordering)
  }
}

abstract class FacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  val name: String

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => b += name,
      b => Option(shapeExtension.extension).foreach(DataNodeEmitter(_, ordering)(spec.eh).emit(b))
    )
  }

  override def position(): Position = pos(shapeExtension.annotations)
}

case class OasFacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends FacetsInstanceEmitter(shapeExtension, ordering) {

  override val name: String = s"facet-${shapeExtension.definedBy.name.value()}".asOasExtension
}

case class RamlFacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends FacetsInstanceEmitter(shapeExtension, ordering) {

  override val name: String = shapeExtension.definedBy.name.value()
}
