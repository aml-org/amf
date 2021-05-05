package amf.plugins.document.webapi.contexts

import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
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
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.models.UnionShape
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

abstract class SpecEmitterContext(val eh: ErrorHandler, refEmitter: RefEmitter, val options: ShapeRenderOptions) {

  def ref(b: PartBuilder, url: String): Unit = refEmitter.ref(url, b)

  def localReference(reference: Linkable): PartEmitter

  val vendor: Vendor

  val factory: SpecEmitterFactory

  def getRefEmitter: RefEmitter = refEmitter

  def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType = YType.Str): EntryEmitter =
    ArrayEmitter(key, f, ordering, forceMultiple = false, valuesTag)

  def oasTypePropertyEmitter(typeName: String, shape: Shape): MapEntryEmitter = {
    shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
      case Some(lexicalInfo) =>
        MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start)
      case None =>
        MapEntryEmitter("type", typeName)
    }
  }

  def ramlTypePropertyEmitter(typeName: String, shape: Shape): Option[MapEntryEmitter] = {
    shape.fields.?(NodeShapeModel.Inherits) match {
      case None =>
        // If the type is union and anyOf is empty it isn't resolved and type will be emitted in the UnionEmitter
        if (typeName == "union" && shape.asInstanceOf[UnionShape].anyOf.nonEmpty) None
        else {
          shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
            case Some(lexicalInfo) =>
              Some(MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start))
            case _ => None
          }
        }
      case _ => None
    }
  }

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

  def externalLink(link: Linkable, refs: Seq[BaseUnit]): Option[BaseUnit] = {
    link.linkTarget match {
      case Some(element) =>
        val linkTarget = element.id
        refs.find {
          case fragment: EncodesModel =>
            Option(fragment.encodes).exists(_.id == linkTarget)
          case library: DeclaresModel =>
            library.declares.exists(_.id == linkTarget)
          case _ => false
        }

      case _ => None
    }
  }
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