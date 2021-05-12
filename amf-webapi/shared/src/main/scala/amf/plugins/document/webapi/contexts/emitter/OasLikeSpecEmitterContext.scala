package amf.plugins.document.webapi.contexts.emitter

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters.ArrayEmitter
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.utils._
import amf.plugins.document.webapi.contexts.emitter.oas.OasRefEmitter
import amf.plugins.document.webapi.contexts.{SpecEmitterContext, SpecEmitterFactory}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.OasLikeShapeEmitterContextAdapter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  OasAnnotationEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.{RefEmitter, SchemaVersion}
import org.yaml.model.YType

import scala.util.matching.Regex

abstract class OasLikeSpecEmitterFactory(implicit val spec: OasLikeSpecEmitterContext) extends SpecEmitterFactory {

  protected implicit val shapeCtx = OasLikeShapeEmitterContextAdapter(spec)

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field] = Nil,
                   references: Seq[BaseUnit],
                   pointer: Seq[String] = Nil,
                   schemaPath: Seq[(String, String)] = Nil): Seq[Emitter]

  def recursiveShapeEmitter(shape: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): EntryEmitter

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = OasAnnotationEmitter.apply
}

abstract class OasLikeSpecEmitterContext(eh: ErrorHandler,
                                         refEmitter: RefEmitter = OasRefEmitter,
                                         options: ShapeRenderOptions = ShapeRenderOptions())
    extends SpecEmitterContext(eh, refEmitter, options) {
  override def schemaVersion: SchemaVersion
  def schemasDeclarationsPath: String

  def nameRegex: Regex

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType): EntryEmitter =
    ArrayEmitter(key, f, ordering, forceMultiple = true, valuesTag)

  val factory: OasLikeSpecEmitterFactory

  val anyOfKey: String = "union".asOasExtension
}
