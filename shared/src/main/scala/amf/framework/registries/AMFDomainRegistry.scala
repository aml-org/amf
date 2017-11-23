package amf.framework.registries

import amf.framework.metamodel.Obj
import amf.framework.metamodel.document.{DocumentModel, FragmentModel, ModuleModel}
import amf.framework.model.domain.{AnnotationGraphLoader, LexicalInformation}

import scala.collection.mutable

object AMFDomainRegistry {
  val annotationsRegistry: mutable.HashMap[String,AnnotationGraphLoader] = mutable.HashMap(
    "lexical" -> LexicalInformation
  )
  val metadataRegistry: mutable.HashMap[String,Obj] = mutable.HashMap(
    defaultIri(DocumentModel) -> DocumentModel,
    defaultIri(ModuleModel)   -> ModuleModel,
    defaultIri(FragmentModel) -> FragmentModel
  )

  def registerAnnotation(annotation: String, annotationGraphLoader: AnnotationGraphLoader) = {
    annotationsRegistry.put(annotation, annotationGraphLoader)
  }

  protected def defaultIri(metadata: Obj) = metadata.`type`.head.iri()
}
