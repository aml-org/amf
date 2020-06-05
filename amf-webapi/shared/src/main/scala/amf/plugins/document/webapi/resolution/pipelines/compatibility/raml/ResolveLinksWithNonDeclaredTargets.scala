package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Linkable
import amf.plugins.document.webapi.resolution.pipelines.compatibility.common.AmfElementLinkResolutionStage

// TODO we need to do this because some links might point to properties within declared elements
class ResolveLinksWithNonDeclaredTargets()(override implicit val errorHandler: ErrorHandler)
    extends AmfElementLinkResolutionStage {

  override def selector[T <: BaseUnit](l: Linkable, model: T): Boolean = {
    model match {
      case doc: Document =>
        val targetId = l.effectiveLinkTarget().id
        !doc.declares.exists(_.id == targetId)
      case _ => false
    }
  }
}
