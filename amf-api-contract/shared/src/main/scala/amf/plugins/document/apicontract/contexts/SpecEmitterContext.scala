package amf.plugins.document.apicontract.contexts

import amf.core.client.scala.config.ShapeRenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.Vendor
import amf.core.internal.render.BaseEmitters.ArrayEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  AnnotationTypeEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.apicontract.parser.spec.domain._
import amf.plugins.domain.apicontract.models._
import amf.plugins.domain.apicontract.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

abstract class SpecEmitterContext(val eh: AMFErrorHandler,
                                  private val refEmitter: RefEmitter,
                                  val options: ShapeRenderOptions)
    extends DeclarationEmissionDecorator {

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
