package amf.plugins.document.webapi.contexts.emitter

import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.utils._
import amf.plugins.document.webapi.contexts.emitter.oas.OasRefEmitter
import amf.plugins.document.webapi.contexts.{RefEmitter, SpecEmitterContext, SpecEmitterFactory}
import amf.plugins.document.webapi.parser.OasTypeDefStringValueMatcher
import amf.plugins.document.webapi.parser.spec.async.emitters.Draft7ExampleEmitters
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.{OasRecursiveShapeEmitter, OasTypeEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.{
  AnnotationEmitter,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  OasAnnotationEmitter
}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{OasExampleEmitters, OasLikeExampleEmitters}
import amf.plugins.domain.shapes.models.Example

import scala.collection.mutable

abstract class OasLikeSpecEmitterFactory(implicit val spec: OasLikeSpecEmitterContext) extends SpecEmitterFactory {

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field] = Nil,
                   references: Seq[BaseUnit],
                   pointer: Seq[String] = Nil,
                   schemaPath: Seq[(String, String)] = Nil): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

  def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    OasRecursiveShapeEmitter.apply

  def exampleEmitter: (Boolean, Option[Example], SpecOrdering, Seq[Example], Seq[BaseUnit]) => OasLikeExampleEmitters =
    (isHeader, exampleOption, ordering, extensions, references) =>
      if (spec.schemaVersion == JSONSchemaDraft7SchemaVersion)
        Draft7ExampleEmitters.apply(exampleOption.toSeq ++ extensions, ordering, references)
      else
        OasExampleEmitters.apply(isHeader, exampleOption, ordering, extensions, references)

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = OasAnnotationEmitter.apply
}

abstract class OasLikeSpecEmitterContext(eh: ErrorHandler,
                                         refEmitter: RefEmitter = OasRefEmitter,
                                         options: ShapeRenderOptions = ShapeRenderOptions())
    extends SpecEmitterContext(eh, refEmitter, options) {
  def schemaVersion: JSONSchemaVersion
  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel.option(), Nil)

  val factory: OasLikeSpecEmitterFactory

  val jsonPointersMap: mutable.Map[String, String] = mutable.Map() // id -> pointer

  val anyOfKey: String = "union".asOasExtension

  def typeDefMatcher: OasTypeDefStringValueMatcher
}
