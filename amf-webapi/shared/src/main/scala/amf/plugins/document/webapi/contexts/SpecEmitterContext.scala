package amf.plugins.document.webapi.contexts

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  AnnotationTypeEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

trait DeclarationEmissionDecorator {
  private var emittingDeclarations: Boolean = false

  def runAsDeclarations(fn: () => Unit): Unit = {
    emittingDeclarations = true
    fn()
    emittingDeclarations = false
  }

  def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] = {
    if (!emittingDeclarations) elements
    else elements.filter(!_.fromLocal())
  }
}

abstract class SpecEmitterContext(val eh: ErrorHandler,
                                  private val refEmitter: RefEmitter,
                                  val options: ShapeRenderOptions) {

  def ref(b: PartBuilder, url: String): Unit = refEmitter.ref(url, b)

  def schemaVersion: SchemaVersion

  def localReference(reference: Linkable): PartEmitter

  val vendor: Vendor

  val factory: SpecEmitterFactory

  def getRefEmitter: RefEmitter = refEmitter

  def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType = YType.Str): EntryEmitter =
    ArrayEmitter(key, f, ordering, forceMultiple = false, valuesTag)
}

trait SpecEmitterFactory {
  def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter

  def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter

  def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter

  def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter

  def parametrizedSecurityEmitter: (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter

  def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter

  def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter

  def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter
}
