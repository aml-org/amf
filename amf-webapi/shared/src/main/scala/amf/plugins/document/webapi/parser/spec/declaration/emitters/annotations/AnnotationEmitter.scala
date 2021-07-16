package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.model.domain._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.Position
import amf.plugins.document.graph.emitter.VendorExtensionEmitter
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import org.yaml.model.YDocument.EntryBuilder

/**
  *
  */
case class AnnotationsEmitter(element: CustomizableElement, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = {
    val regularAnnotations = element.customDomainProperties
      .filter(!isOrphanOasExtension(_))
      .map(spec.factory.annotationEmitter(_, ordering))

    element match {
      case d: DomainElement =>
        val extensionsAnnotations = VendorExtensionEmitter.emit(d, spec.factory.annotationKeyDecorator)

        ordering.sorted(regularAnnotations ++ extensionsAnnotations)
      case _ => Nil
    }
  }

  private def isOrphanOasExtension(customProperty: DomainExtension) = {
    customProperty.extension.annotations.contains(classOf[OrphanOasExtension])
  }
}

abstract class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
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
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "x-" + domainExtension.name.value()
}

case class RamlAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "(" + domainExtension.name.value() + ")"
}
