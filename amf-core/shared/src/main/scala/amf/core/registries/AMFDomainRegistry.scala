package amf.core.registries

import amf.core.annotations._
import amf.core.metamodel.Obj
import amf.core.metamodel.document._
import amf.core.metamodel.domain.extensions.{
  CustomDomainPropertyModel,
  DomainExtensionModel,
  PropertyShapeModel,
  ShapeExtensionModel
}
import amf.core.metamodel.domain.templates.VariableValueModel
import amf.core.metamodel.domain.{ExternalDomainElementModel, RecursiveShapeModel, ShapeModel}
import amf.core.model.domain.{AmfObject, AnnotationGraphLoader}
import amf.core.parser.Annotations

import scala.collection.mutable

trait AMFDomainEntityResolver {
  def findType(typeString: String): Option[Obj]

  def buildType(modelType: Obj): Option[(Annotations) => AmfObject]
}

object AMFDomainRegistry {

  def findType(typeString: String): Option[Obj] =
    metadataResolverRegistry.toStream
      .map(_.findType(typeString))
      .filter(_.isDefined)
      .map(_.get)
      .headOption

  def buildType(modelType: Obj): Option[(Annotations) => AmfObject] =
    metadataResolverRegistry.toStream
      .map(_.buildType(modelType))
      .filter(_.isDefined)
      .map(_.get)
      .headOption

  val annotationsRegistry: mutable.HashMap[String, AnnotationGraphLoader] = mutable.HashMap(
    "lexical"            -> LexicalInformation,
    "source-vendor"      -> SourceVendor,
    "single-value-array" -> SingleValueArray,
    "aliases-array"      -> Aliases,
    "synthesized-field"  -> SynthesizedField
  )
  val metadataRegistry: mutable.HashMap[String, Obj] = mutable.HashMap(
    defaultIri(DocumentModel)              -> DocumentModel,
    defaultIri(ModuleModel)                -> ModuleModel,
    defaultIri(VariableValueModel)         -> VariableValueModel,
    defaultIri(SourceMapModel)             -> SourceMapModel,
    defaultIri(ShapeModel)                 -> ShapeModel,
    defaultIri(RecursiveShapeModel)        -> RecursiveShapeModel,
    defaultIri(PropertyShapeModel)         -> PropertyShapeModel,
    defaultIri(ShapeExtensionModel)        -> ShapeExtensionModel,
    defaultIri(CustomDomainPropertyModel)  -> CustomDomainPropertyModel,
    defaultIri(ExternalFragmentModel)      -> ExternalFragmentModel,
    defaultIri(ExternalDomainElementModel) -> ExternalDomainElementModel,
    defaultIri(DomainExtensionModel)       -> DomainExtensionModel
  )

  val metadataResolverRegistry: mutable.ListBuffer[AMFDomainEntityResolver] = mutable.ListBuffer.empty

  def registerAnnotation(annotation: String, annotationGraphLoader: AnnotationGraphLoader) = {
    annotationsRegistry.put(annotation, annotationGraphLoader)
  }

  def registerModelEntity(entity: Obj) = {
    metadataRegistry.put(defaultIri(entity), entity)
  }

  def registerModelEntityResolver(resolver: AMFDomainEntityResolver) = {
    metadataResolverRegistry.append(resolver)
  }

  protected def defaultIri(metadata: Obj) = metadata.`type`.head.iri()
}
