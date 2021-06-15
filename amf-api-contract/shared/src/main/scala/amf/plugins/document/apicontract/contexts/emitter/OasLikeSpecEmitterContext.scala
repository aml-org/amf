package amf.plugins.document.apicontract.contexts.emitter

import amf.core.client.scala.config.ShapeRenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.ArrayEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.{SpecEmitterContext, SpecEmitterFactory}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  OasAnnotationEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.{OasRefEmitter, RefEmitter, SchemaVersion}
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

abstract class OasLikeSpecEmitterContext(eh: AMFErrorHandler,
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
