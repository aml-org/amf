package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.security.{ParametrizedSecurityScheme, SecurityRequirement}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.client.scala.model.domain.{CustomizableElement, DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters.ArrayEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, RefEmitter, TagToReferenceEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationEmitter, FacetsInstanceEmitter}
import amf.shapes.internal.spec.contexts.DeclarationEmissionDecorator
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

abstract class SpecEmitterContext(val eh: AMFErrorHandler,
                                  private val refEmitter: RefEmitter,
                                  val renderConfig: RenderConfiguration)
    extends DeclarationEmissionDecorator {

  val options: RenderOptions = renderConfig.renderOptions

  def ref(b: PartBuilder, url: String): Unit = refEmitter.ref(url, b)

  def schemaVersion: SchemaVersion

  def localReference(reference: Linkable): PartEmitter

  val spec: Spec

  val factory: SpecEmitterFactory

  def getRefEmitter: RefEmitter = refEmitter

  def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType = YType.Str): EntryEmitter =
    ArrayEmitter(key, f, ordering, forceMultiple = false, valuesTag)
}

trait SpecEmitterFactory {
  def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter

  def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter

  def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter

  def annotationEmitter: (CustomizableElement, DomainExtension, SpecOrdering) => AnnotationEmitter

  def parametrizedSecurityEmitter: (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter

  def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter

  def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter

  def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter
}
