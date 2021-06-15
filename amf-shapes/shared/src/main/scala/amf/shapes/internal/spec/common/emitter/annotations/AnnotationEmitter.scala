package amf.shapes.internal.spec.common.emitter.annotations

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.CustomizableElement
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.{DataNodeEmitter, ShapeEmitterContext}
import org.yaml.model.YDocument.EntryBuilder

/**
  *
  */
case class AnnotationsEmitter(element: CustomizableElement, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] =
    element.customDomainProperties
      .filter(!isOrphanOasExtension(_))
      .map(spec.annotationEmitter(_, ordering))

  private def isOrphanOasExtension(customProperty: DomainExtension) = {
    customProperty.extension.annotations.contains(classOf[OrphanOasExtension])
  }
}

abstract class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  val name: String

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => b += name,
      b => Option(domainExtension.extension).foreach { DataNodeEmitter(_, ordering)(spec.eh).emit(b) }
    )
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class OasAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "x-" + domainExtension.name.value()
}

case class RamlAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "(" + domainExtension.name.value() + ")"
}
