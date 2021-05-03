package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.parser.Position
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
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
