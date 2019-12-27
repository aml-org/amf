package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.models.{Tag, WebApi}

class MandatoryDocumentationTitle()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  var tagCounter = 0

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case d: Document if d.encodes.isInstanceOf[WebApi] =>
        try {
          ensureDocumentationTitle(d.encodes.asInstanceOf[WebApi])
        } catch {
          case _: Throwable => // ignore: we don't want this to break anything
        }
        model
      case _ => model
    }
  }

  private def ensureDocumentationTitle(api: WebApi): Unit = {
    if (missingTitle(api)) {
      addMissingTitle(api)
    }

    api.documentations.foreach { documentation =>
      if (documentation.title.isNullOrEmpty) {
        documentation.url.option() match {
          case Some(url) => documentation.withTitle(url)
          case _         => documentation.withTitle("generated")
        }
      }
      documentation.fields.removeField(CreativeWorkModel.Url)
    }

    api.tags.foreach { tag =>
      ensureDocumentationTitleInTag(tag)
    }
  }

  private def missingTitle(webApi: WebApi): Boolean = webApi.name.option().isEmpty

  private def addMissingTitle(webApi: WebApi): Unit = {
    if (webApi.name.isNullOrEmpty) {
      webApi.withName("generated")
    }
  }

  private def ensureDocumentationTitleInTag(tag: Tag): Unit = {
    if (Option(tag.documentation).isDefined) {
      val documentationTitle = tag.name.option().getOrElse {
        tagCounter += 1
        s"Tag $tagCounter documentation"
      }
      tag.documentation.withTitle(documentationTitle)
    }
  }
}
