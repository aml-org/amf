package amf.plugins.document.apicontract.contexts.emitter.async

import amf.core.client.scala.config.ShapeRenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.{AsyncApi20, Vendor}
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationTypeEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.{OasRecursiveShapeEmitter, OasTypeEmitter}
import amf.plugins.document.apicontract.parser.spec.domain.{
  AbstractSecurityRequirementEmitter,
  ParametrizedSecuritySchemeEmitter
}
import amf.plugins.document.apicontract.parser.spec.oas.emitters.OasSecurityRequirementEmitter
import amf.plugins.domain.apicontract.models.Parameter
import amf.plugins.domain.apicontract.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder

import scala.util.matching.Regex

abstract class AsyncSpecEmitterFactory(override implicit val spec: AsyncSpecEmitterContext)
    extends OasLikeSpecEmitterFactory {

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    AsyncDeclaredTypesEmitters.obtainEmitter

  def recursiveShapeEmitter(shape: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): EntryEmitter =
    OasRecursiveShapeEmitter(shape, ordering, schemaPath)

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field] = Nil,
                   references: Seq[BaseUnit],
                   pointer: Seq[String] = Nil,
                   schemaPath: Seq[(String, String)] = Nil): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
}

case class Async20SpecEmitterFactory(override val spec: AsyncSpecEmitterContext)
    extends AsyncSpecEmitterFactory()(spec) {
  //TODO ASYNC complete this
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

abstract class AsyncSpecEmitterContext(eh: AMFErrorHandler,
                                       refEmitter: RefEmitter = AsyncRefEmitter,
                                       options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasLikeSpecEmitterContext(eh, refEmitter, options) {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override val factory: AsyncSpecEmitterFactory
}

class Async20SpecEmitterContext(eh: AMFErrorHandler,
                                refEmitter: RefEmitter = AsyncRefEmitter,
                                options: ShapeRenderOptions = ShapeRenderOptions(),
                                val schemaVersion: SchemaVersion = JSONSchemaDraft7SchemaVersion)
    extends AsyncSpecEmitterContext(eh, refEmitter, options) {

  override val nameRegex: Regex = """^[a-zA-Z0-9\.\-_]+$""".r

  override val factory: AsyncSpecEmitterFactory = Async20SpecEmitterFactory(this)
  override val vendor: Vendor                   = AsyncApi20
  override def schemasDeclarationsPath: String  = "/definitions/"
}

object AsyncRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
