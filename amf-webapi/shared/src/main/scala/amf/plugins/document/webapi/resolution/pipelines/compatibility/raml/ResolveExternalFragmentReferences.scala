package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.annotations.ExternalFragmentRef
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Linkable
import amf.plugins.document.webapi.resolution.pipelines.compatibility.common.AmfElementLinkResolutionStage

// TODO we need to do this because some links might point to properties within declared elements
class ResolveExternalFragmentReferences()(override implicit val errorHandler: ErrorHandler)
    extends AmfElementLinkResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    super.resolve(model)
    model match {
      case doc: Document =>
        val newDeclares = doc.declares.map {
          case l: Linkable if l.isLink && selector(l, model) =>
            resolver.transform(l).getOrElse(l)
          case d => d
        }
        doc.withDeclares(newDeclares)
    }
    model
  }

  override def selector[T <: BaseUnit](l: Linkable, model: T): Boolean = {
    l.annotations.contains(classOf[ExternalFragmentRef])
  }
}
