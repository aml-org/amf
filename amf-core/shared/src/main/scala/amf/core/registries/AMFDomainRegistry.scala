package amf.core.registries

import amf.core.annotations._
import amf.core.metamodel.Obj
import amf.core.metamodel.document.{DocumentModel, ModuleModel, SourceMapModel}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel, PropertyShapeModel, ShapeExtensionModel}
import amf.core.metamodel.domain.templates.VariableValueModel
import amf.core.model.domain.AnnotationGraphLoader

import scala.collection.mutable

object AMFDomainRegistry {
  val annotationsRegistry: mutable.HashMap[String,AnnotationGraphLoader] = mutable.HashMap(
    "lexical"            -> LexicalInformation,
    "source-vendor"      -> SourceVendor,
    "single-value-array" -> SingleValueArray,
    "aliases-array"      -> Aliases,
    "synthesized-field"  -> SynthesizedField
  )
  val metadataRegistry: mutable.HashMap[String,Obj] = mutable.HashMap(
    defaultIri(DocumentModel)             -> DocumentModel,
    defaultIri(ModuleModel)               -> ModuleModel,
    defaultIri(VariableValueModel)        -> VariableValueModel,
    defaultIri(SourceMapModel)            -> SourceMapModel,
    defaultIri(ShapeModel)                -> ShapeModel,
    defaultIri(PropertyShapeModel)        -> PropertyShapeModel,
    defaultIri(ShapeExtensionModel)       -> ShapeExtensionModel,
    defaultIri(CustomDomainPropertyModel) -> CustomDomainPropertyModel,
    defaultIri(DomainExtensionModel)      -> DomainExtensionModel
  )

  def registerAnnotation(annotation: String, annotationGraphLoader: AnnotationGraphLoader) = {
    annotationsRegistry.put(annotation, annotationGraphLoader)
  }

  def registerModelEntity(entity: Obj) = {
    metadataRegistry.put(defaultIri(entity), entity)
  }

  protected def defaultIri(metadata: Obj) = metadata.`type`.head.iri()
}
