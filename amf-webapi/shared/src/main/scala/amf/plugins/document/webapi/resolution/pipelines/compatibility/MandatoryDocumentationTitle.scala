package amf.plugins.document.webapi.resolution.pipelines.compatibility
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{Tag, WebApi}

class MandatoryDocumentationTitle()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  var tagCounter = 0

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case d: Document if d.encodes.isInstanceOf[WebApi] =>
        ensureDocumentationTitle(d.encodes.asInstanceOf[WebApi])
        model
      case _ => model
    }
  }

  protected def ensureDocumentationTitle(api: WebApi) = {
    if (missingTitle(api)) {
      addMissingTitle(api)
    }
    api.documentations.foreach { documentation =>
      if (documentation.title.isNullOrEmpty) {
        documentation.withTitle("")
      }
    }
    api.tags.foreach { tag =>
      ensureDocumentationTitleInTag(tag)
    }
  }

  protected def missingTitle(webApi: WebApi): Boolean = webApi.name.option().isEmpty


  protected def addMissingTitle(webApi: WebApi): Unit = {
    if (webApi.name.isNullOrEmpty) {
      webApi.withName("")
    }

  }


  protected def ensureDocumentationTitleInTag(tag: Tag): Unit = {
    if (Option(tag.documentation).isDefined) {
      val documentationTitle = tag.name.option().getOrElse {
        tagCounter += 1
        s"Tag ${tagCounter} documentation"
      }
      tag.documentation.withTitle(documentationTitle)
    }
  }


}
