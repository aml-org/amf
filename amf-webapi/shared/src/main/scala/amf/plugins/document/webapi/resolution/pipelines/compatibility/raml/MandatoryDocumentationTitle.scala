package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.models.{DocumentedElement, Tag}

class MandatoryDocumentationTitle()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  var tagCounter = 0

  override def resolve[T <: BaseUnit](model: T): T = {
    // Have to filter for null documentations as the documentation method of domainElements does not return Option. It can return null
    // TODO: keeping separate treatment for Tags to keep backwards compatibility.
    extractDocumentedElements(model).foreach {
      case t: Tag => generateDocumentationTitleForTag(t)
      case other  => other.documentations.filter(x => x != null).foreach(generateDocumentationTitle)
    }
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        val webApi = doc.encodes.asInstanceOf[Api]
        ensureBaseApiTitle(webApi)
        model
      case _ => model
    }
  }

  private def extractDocumentedElements[T <: BaseUnit](model: T) =
    model
      .iterator()
      .collect { case d: DocumentedElement => d }

  private def generateDocumentationTitle(documentation: CreativeWork): Unit = {
    if (needsATitle(documentation)) {
      documentation.url.option() match {
        case Some(url) => documentation.withTitle(url)
        case None      => documentation.withTitle("generated")
      }
    }
    documentation.fields.removeField(CreativeWorkModel.Url)
  }

  private def generateDocumentationTitleForTag(tag: Tag): Unit = {
    Option(tag.documentation) match {
      case Some(doc) if needsATitle(doc) =>
        val documentationTitle = tag.name.option().getOrElse {
          tagCounter += 1
          s"Tag $tagCounter documentation"
        }
        tag.documentation.withTitle(documentationTitle)
      case None => // ignore
    }
  }

  private def needsATitle(documentation: CreativeWork) = {
    Option(documentation.title) match {
      case Some(strField) => strField.isNullOrEmpty
      case None           => false
    }
  }

  private def ensureBaseApiTitle(api: Api) = {
    if (missingTitle(api)) {
      api.withName("generated")
    }
  }

  private def missingTitle(api: Api): Boolean = api.name.option().isEmpty
}
