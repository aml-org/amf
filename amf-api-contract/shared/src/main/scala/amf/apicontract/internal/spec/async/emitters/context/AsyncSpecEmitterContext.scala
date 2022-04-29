package amf.apicontract.internal.spec.async.emitters.context

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.security.{ParametrizedSecurityScheme, SecurityRequirement}
import amf.apicontract.internal.spec.async.emitters.document.AsyncDeclaredTypesEmitters
import amf.apicontract.internal.spec.common.emitter.{
  AbstractSecurityRequirementEmitter,
  AnnotationTypeEmitter,
  ParametrizedSecuritySchemeEmitter
}
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.apicontract.internal.spec.oas.emitter.domain.OasSecurityRequirementEmitter
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.{AsyncApi20, Spec}
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.FacetsInstanceEmitter
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, RefEmitter, TagToReferenceEmitter}
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.emitter.{OasRecursiveShapeEmitter, OasTypeEmitter}
import org.yaml.model.YDocument.PartBuilder

import scala.util.matching.Regex

abstract class AsyncSpecEmitterFactory(override implicit val spec: AsyncSpecEmitterContext)
    extends OasLikeSpecEmitterFactory {

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    (types, references, ordering) =>
      AsyncDeclaredTypesEmitters.obtainEmitter(types, references, ordering, spec.renderConfig)

  def recursiveShapeEmitter(
      shape: RecursiveShape,
      ordering: SpecOrdering,
      schemaPath: Seq[(String, String)]
  ): EntryEmitter =
    OasRecursiveShapeEmitter(shape, ordering, schemaPath)

  def typeEmitters(
      shape: Shape,
      ordering: SpecOrdering,
      ignored: Seq[Field] = Nil,
      references: Seq[BaseUnit],
      pointer: Seq[String] = Nil,
      schemaPath: Seq[(String, String)] = Nil
  ): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
}

case class Async20SpecEmitterFactory(override val spec: AsyncSpecEmitterContext)
    extends AsyncSpecEmitterFactory()(spec) {
  // TODO ASYNC complete this
  override def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter = ???

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter = ???

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter = ???

  override def parametrizedSecurityEmitter
      : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter = ???

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter =
    OasSecurityRequirementEmitter.apply

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = ???

  override def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = ???
}

abstract class AsyncSpecEmitterContext(
    eh: AMFErrorHandler,
    refEmitter: RefEmitter = AsyncRefEmitter,
    config: RenderConfiguration
) extends OasLikeSpecEmitterContext(eh, refEmitter, config) {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override val factory: AsyncSpecEmitterFactory
}

class Async20SpecEmitterContext(
    eh: AMFErrorHandler,
    refEmitter: RefEmitter = AsyncRefEmitter,
    config: RenderConfiguration,
    val schemaVersion: SchemaVersion = JSONSchemaDraft7SchemaVersion
) extends AsyncSpecEmitterContext(eh, refEmitter, config) {

  override val nameRegex: Regex = """^[a-zA-Z0-9\.\-_]+$""".r

  override val factory: AsyncSpecEmitterFactory = Async20SpecEmitterFactory(this)
  override val spec: Spec                       = AsyncApi20
  override def schemasDeclarationsPath: String  = "/definitions/"
}

object AsyncRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
