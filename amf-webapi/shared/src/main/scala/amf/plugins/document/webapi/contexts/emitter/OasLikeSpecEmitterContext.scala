package amf.plugins.document.webapi.contexts.emitter

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
import amf.plugins.document.webapi.contexts.{RefEmitter, SpecEmitterContext, SpecEmitterFactory}
import amf.plugins.document.webapi.parser.OasTypeDefStringValueMatcher
import amf.plugins.document.webapi.parser.spec.async.emitters.Draft6ExamplesEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  OasAnnotationEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{OasExampleEmitters, OasLikeExampleEmitters}
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YType

import scala.collection.mutable

abstract class OasLikeSpecEmitterFactory(implicit val spec: OasLikeSpecEmitterContext) extends SpecEmitterFactory {

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field] = Nil,
                   references: Seq[BaseUnit],
                   pointer: Seq[String] = Nil,
                   schemaPath: Seq[(String, String)] = Nil): Seq[Emitter]

  def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter

  def exampleEmitter: (Boolean, Option[Example], SpecOrdering, Seq[Example], Seq[BaseUnit]) => OasLikeExampleEmitters =
    (isHeader, exampleOption, ordering, extensions, references) =>
      if (spec.schemaVersion == JSONSchemaDraft7SchemaVersion)
        Draft6ExamplesEmitter(exampleOption.toSeq ++ extensions, ordering)
      else
        OasExampleEmitters.apply(isHeader, exampleOption, ordering, extensions, references)

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = OasAnnotationEmitter.apply
}

abstract class OasLikeSpecEmitterContext(eh: ErrorHandler,
                                         refEmitter: RefEmitter = OasRefEmitter,
                                         options: ShapeRenderOptions = ShapeRenderOptions())
    extends SpecEmitterContext(eh, refEmitter, options) {
  def schemaVersion: SchemaVersion
  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType): EntryEmitter =
    ArrayEmitter(key, f, ordering, forceMultiple = true, valuesTag)

  val factory: OasLikeSpecEmitterFactory

  val jsonPointersMap: mutable.Map[String, String] = mutable.Map() // id -> pointer

  val anyOfKey: String = "union".asOasExtension

  def typeDefMatcher: OasTypeDefStringValueMatcher
}
