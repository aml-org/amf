package amf.framework.registries

import amf.framework.metamodel.Obj
import amf.framework.metamodel.document.{DocumentModel, ModuleModel}
import amf.framework.metamodel.domain.templates.VariableValueModel
import amf.framework.model.domain.{AnnotationGraphLoader, LexicalInformation}

import scala.collection.mutable

object AMFDomainRegistry {
  val annotationsRegistry: mutable.HashMap[String,AnnotationGraphLoader] = mutable.HashMap(
    "lexical" -> LexicalInformation
  )
  val metadataRegistry: mutable.HashMap[String,Obj] = mutable.HashMap(
    defaultIri(DocumentModel) -> DocumentModel,
    defaultIri(ModuleModel)   -> ModuleModel,
    defaultIri(VariableValueModel) -> VariableValueModel
  )

  def registerAnnotation(annotation: String, annotationGraphLoader: AnnotationGraphLoader) = {
    annotationsRegistry.put(annotation, annotationGraphLoader)
  }

  def registerModelEntity(entity: Obj) = {
    metadataRegistry.put(defaultIri(entity), entity)
  }

  protected def defaultIri(metadata: Obj) = metadata.`type`.head.iri()
}
