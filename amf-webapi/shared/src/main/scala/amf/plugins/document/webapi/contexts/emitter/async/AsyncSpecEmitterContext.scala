package amf.plugins.document.webapi.contexts.emitter.async

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, PartEmitter, ShapeRenderOptions, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{ErrorHandler, FieldEntry}
import amf.core.remote.{AsyncApi20, Vendor}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.webapi.contexts.{RefEmitter, TagToReferenceEmitter}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.{CommonOasTypeDefMatcher, OasTypeDefStringValueMatcher}
import amf.plugins.domain.webapi.models.Parameter
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder

abstract class AsyncSpecEmitterFactory(override implicit val spec: AsyncSpecEmitterContext)
    extends OasLikeSpecEmitterFactory {
  // TODO ASYNC complete this
}

case class Async20SpecEmitterFactory(override val spec: AsyncSpecEmitterContext)
    extends AsyncSpecEmitterFactory()(spec) {
  //TODO ASYNC complete this
  override def tagToReferenceEmitter: (DomainElement, Option[String], Seq[BaseUnit]) => TagToReferenceEmitter = ???

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter = ???

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter = ???

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = ???

  override def parametrizedSecurityEmitter
    : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter = ???

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => SecurityRequirementEmitter = ???

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = ???

  override def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = ???

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter = ???
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

  // TODO ASYNC check this
  val anyOfKey: String = "union".asAsyncExtension
}

class Async20SpecEmitterContext(eh: ErrorHandler,
                                refEmitter: RefEmitter = AsyncRefEmitter,
                                options: ShapeRenderOptions = ShapeRenderOptions())
    extends AsyncSpecEmitterContext(eh, refEmitter, options) {
  override val factory: AsyncSpecEmitterFactory = Async20SpecEmitterFactory(this)
  override val vendor: Vendor                   = AsyncApi20
  override def schemasDeclarationsPath: String  = "/definitions/"
}

object AsyncRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
