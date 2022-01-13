package amf.shapes.internal.spec.common.emitter.annotations

import amf.aml.internal.registries.AMLRegistry
import amf.aml.internal.semantic.SemanticExtensionsFacade
import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.{CustomizableElement, DataNode}
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationEmitter.ComputeName
import amf.shapes.internal.spec.common.emitter.annotations.OasAnnotationEmitter.computeName
import amf.shapes.internal.spec.common.emitter.{DataNodeEmitter, ShapeEmitterContext}
import org.yaml.model.YDocument.EntryBuilder

/**
  *
  */
case class AnnotationsEmitter(element: CustomizableElement, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] =
    element.customDomainProperties
      .filter(!isOrphanOasExtension(_))
      .map(spec.annotationEmitter(element, _, ordering))

  private def isOrphanOasExtension(customProperty: DomainExtension) = {
    Option(customProperty.extension).map(_.annotations.contains(classOf[OrphanOasExtension])).getOrElse(false)
  }
}

object AnnotationEmitter {
  type ComputeName = DomainExtension => String
}

class AstAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering, computeName: ComputeName)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    Option(domainExtension.extension).foreach(emitAst(b, _))
  }

  protected def emitAst(b: EntryBuilder, extension: DataNode): Unit = {
    b.complexEntry(
      b => b += computeName(domainExtension),
      b => DataNodeEmitter(extension, ordering)(spec.eh).emit(b)
    )
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class AnnotationEmitter(element: CustomizableElement,
                             domainExtension: DomainExtension,
                             ordering: SpecOrdering,
                             computeName: ComputeName)(implicit spec: ShapeEmitterContext)
    extends AstAnnotationEmitter(domainExtension, ordering, computeName) {

  override def emit(b: EntryBuilder): Unit = {
    Option(domainExtension.extension) match {
      case Some(node) => emitAst(b, node)
      case None       => emitSemanticExtension(b)
    }
  }

  private def emitSemanticExtension(b: EntryBuilder): Unit = {
    SemanticExtensionsFacade(spec.config)
      .render(computeName(domainExtension), domainExtension, element.typeIris, ordering, spec.options)
      .foreach(_.emit(b))
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class RamlScalarAnnotationEmitter(extension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {

  private val name = RamlAnnotationEmitter.computeName(extension)

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => b += name,
      b =>
        Option(extension.extension).foreach { ast =>
          DataNodeEmitter(ast, ordering)(spec.eh).emit(b)
      }
    )
  }

  override def position(): Position = pos(extension.annotations)
}

object OasAstAnnotationEmitter {
  private val computeName: ComputeName = ext => s"x-${ext.name.value()}"

  def apply(domainExtension: DomainExtension, ordering: SpecOrdering)(
      implicit spec: ShapeEmitterContext): AstAnnotationEmitter = {
    new AstAnnotationEmitter(domainExtension, ordering, computeName)
  }
}

object OasAnnotationEmitter {

  private val computeName: ComputeName = ext => s"x-${ext.name.value()}"

  def apply(element: CustomizableElement, domainExtension: DomainExtension, ordering: SpecOrdering)(
      implicit spec: ShapeEmitterContext) = {
    AnnotationEmitter(element, domainExtension, ordering, computeName)
  }
}

object RamlAnnotationEmitter {

  val computeName: ComputeName = ext => s"(${ext.name.value()})"

  def apply(element: CustomizableElement, domainExtension: DomainExtension, ordering: SpecOrdering)(
      implicit spec: ShapeEmitterContext) = {
    AnnotationEmitter(element, domainExtension, ordering, computeName)
  }
}
