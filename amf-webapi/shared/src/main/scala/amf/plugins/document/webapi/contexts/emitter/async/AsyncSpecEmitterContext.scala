package amf.plugins.document.webapi.contexts.emitter.async

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, PartEmitter, ShapeRenderOptions, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.{AsyncApi20, Vendor}
import amf.plugins.document.webapi.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.webapi.contexts.{RefEmitter, TagToReferenceEmitter}
import amf.plugins.document.webapi.parser.spec.async.emitters.Draft7ExampleEmitters
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.{
  AbstractSecurityRequirementEmitter,
  ParametrizedSecuritySchemeEmitter
}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{
  OasExampleEmitters,
  OasLikeExampleEmitters,
  OasSecurityRequirementEmitter
}
import amf.plugins.document.webapi.parser.{CommonOasTypeDefMatcher, OasTypeDefStringValueMatcher}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.Parameter
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder

abstract class AsyncSpecEmitterFactory(override implicit val spec: AsyncSpecEmitterContext)
    extends OasLikeSpecEmitterFactory {

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    AsyncDeclaredTypesEmitters.obtainEmitter

}

case class Async20SpecEmitterFactory(override val spec: AsyncSpecEmitterContext)
    extends AsyncSpecEmitterFactory()(spec) {
  //TODO ASYNC complete this
  override def tagToReferenceEmitter: (DomainElement, Option[String], Seq[BaseUnit]) => TagToReferenceEmitter = ???

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter = ???

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter = ???

  override def parametrizedSecurityEmitter
    : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter = ???

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter =
    OasSecurityRequirementEmitter.apply

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = ???

  override def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = ???
}

abstract class AsyncSpecEmitterContext(eh: ErrorHandler,
                                       refEmitter: RefEmitter = AsyncRefEmitter,
                                       options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasLikeSpecEmitterContext(eh, refEmitter, options) {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel.option(), Nil)

  override val factory: AsyncSpecEmitterFactory

  val typeDefMatcher: OasTypeDefStringValueMatcher = CommonOasTypeDefMatcher
}

class Async20SpecEmitterContext(eh: ErrorHandler,
                                refEmitter: RefEmitter = AsyncRefEmitter,
                                options: ShapeRenderOptions = ShapeRenderOptions(),
                                val schemaVersion: JSONSchemaVersion = JSONSchemaDraft7SchemaVersion)
    extends AsyncSpecEmitterContext(eh, refEmitter, options) {
  override val factory: AsyncSpecEmitterFactory = Async20SpecEmitterFactory(this)
  override val vendor: Vendor                   = AsyncApi20
  override def schemasDeclarationsPath: String  = "/definitions/"
}

object AsyncRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
