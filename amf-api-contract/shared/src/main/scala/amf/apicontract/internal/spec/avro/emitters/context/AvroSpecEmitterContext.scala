package amf.apicontract.internal.spec.avro.emitters.context

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.security.{ParametrizedSecurityScheme, SecurityRequirement}
import amf.apicontract.internal.spec.avro.emitters.document.AvroDeclaredTypesEmitters
import amf.apicontract.internal.spec.avro.emitters.domain.AvroShapeEmitter
import amf.apicontract.internal.spec.common.emitter.{
  AbstractSecurityRequirementEmitter,
  AnnotationTypeEmitter,
  ParametrizedSecuritySchemeEmitter
}
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.{AvroSchema, Spec}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.FacetsInstanceEmitter
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, RefEmitter, TagToReferenceEmitter}
import amf.shapes.internal.spec.common.{SchemaVersion, AVROSchema => AVROSchemaVersion}

import scala.util.matching.Regex

class AvroSpecEmitterContext(
    eh: AMFErrorHandler,
    refEmitter: RefEmitter = AvroRefEmitter,
    val config: RenderConfiguration
) extends OasLikeSpecEmitterContext(eh, refEmitter, config) {

  override val factory: AvroSpecEmitterFactory = new AvroSpecEmitterFactory()(this)
  val spec: Spec                               = AvroSchema
  def schemasDeclarationsPath: String          = "/definitions/"
  override def schemaVersion: SchemaVersion    = AVROSchemaVersion()
  override def nameRegex: Regex                = """^[a-zA-Z0-9.\-_]+$""".r
}

class AvroSpecEmitterFactory(implicit override val spec: AvroSpecEmitterContext) extends OasLikeSpecEmitterFactory {

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    (types, references, ordering) =>
      AvroDeclaredTypesEmitters.obtainEmitter(types, references, ordering, spec.renderConfig)

  def typeEmitters(
      shape: Shape,
      ordering: SpecOrdering,
      ignored: Seq[Field] = Nil,
      references: Seq[BaseUnit],
      pointer: Seq[String] = Nil,
      schemaPath: Seq[(String, String)] = Nil
  ): Seq[Emitter] = {
    implicit val shapeContext: AvroShapeEmitterContext = AvroShapeEmitterContext.fromSpecEmitterContext(spec)
    AvroShapeEmitter(shape, ordering)(shapeContext).entries()
  }

  override def recursiveShapeEmitter(
      shape: RecursiveShape,
      ordering: SpecOrdering,
      schemaPath: Seq[(String, String)]
  ): EntryEmitter = ???

  override def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter = ???

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter = ???

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter = ???

  override def parametrizedSecurityEmitter
      : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter = ???

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter =
    ???

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = ???

  override def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = ???
}

object AvroRefEmitter extends RefEmitter {}
