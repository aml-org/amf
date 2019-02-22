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

  def buildType(modelType: Obj): Option[Annotations => AmfObject]
}

object AMFDomainRegistry {

  def findType(typeString: String): Option[Obj] =
    metadataResolverRegistry.toStream
      .map(_.findType(typeString))
      .filter(_.isDefined)
      .map(_.get)
      .headOption

  def buildType(modelType: Obj): Option[Annotations => AmfObject] =
    metadataResolverRegistry.toStream
      .map(_.buildType(modelType))
      .filter(_.isDefined)
      .map(_.get)
      .headOption

  val annotationsRegistry: mutable.HashMap[String, AnnotationGraphLoader] = map(
    size = 1024,
    "lexical"              -> LexicalInformation,
    "host-lexical"         -> HostLexicalInformation,
    "base-path-lexical"    -> BasePathLexicalInformation,
    "source-vendor"        -> SourceVendor,
    "single-value-array"   -> SingleValueArray,
    "aliases-array"        -> Aliases,
    "synthesized-field"    -> SynthesizedField,
    "default-node"         -> DefaultNode,
    "data-node-properties" -> DataNodePropertiesAnnotations,
    "resolved-link"        -> ResolvedLinkAnnotation,
    "null-security"        -> NullSecurity
  )

  val metadataRegistry: mutable.Map[String, Obj] = map(
    size = 1024,
    defaultIri(DocumentModel)              -> DocumentModel,
    defaultIri(ModuleModel)                -> ModuleModel,
    defaultIri(VariableValueModel)         -> VariableValueModel,
    defaultIri(SourceMapModel)             -> SourceMapModel,
    defaultIri(RecursiveShapeModel)        -> RecursiveShapeModel,
    defaultIri(PropertyShapeModel)         -> PropertyShapeModel,
    defaultIri(ShapeExtensionModel)        -> ShapeExtensionModel,
    defaultIri(CustomDomainPropertyModel)  -> CustomDomainPropertyModel,
    defaultIri(ExternalFragmentModel)      -> ExternalFragmentModel,
    defaultIri(ExternalDomainElementModel) -> ExternalDomainElementModel,
    defaultIri(DomainExtensionModel)       -> DomainExtensionModel
  )

  def map[A, B](size: Int, elems: (A, B)*): mutable.HashMap[A, B] = {
    val r = new mutable.HashMap[A, B] {
      override def initialSize: Int = size
    }
    r ++= elems
  }

  val metadataResolverRegistry: mutable.ListBuffer[AMFDomainEntityResolver] = mutable.ListBuffer.empty

  def registerAnnotation(a: String, agl: AnnotationGraphLoader): Option[AnnotationGraphLoader] =
    annotationsRegistry.put(a, agl)

  def unregisterAnnotaion(a: String): Unit =  annotationsRegistry.remove(a)

  def registerModelEntity(entity: Obj): Option[Obj] = {
    metadataRegistry.put(defaultIri(entity), entity)
  }

  def unregisterModelEntity(entity: Obj): Unit = {
    metadataRegistry.remove(defaultIri(entity))
  }


  def registerModelEntityResolver(resolver: AMFDomainEntityResolver): Unit = metadataResolverRegistry.append(resolver)

  def unregisterModelEntityResolver(resolver: AMFDomainEntityResolver): Unit = metadataResolverRegistry.-=(resolver)

  protected def defaultIri(metadata: Obj): String = metadata.`type`.head.iri()
}
