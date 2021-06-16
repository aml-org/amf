package amf.apicontract.internal.transformation.compatibility.raml

import amf.apicontract.internal.transformation.compatibility.common.AmfElementLinkResolutionStage
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Linkable

// TODO we need to do this because some links might point to properties within declared elements
class ResolveLinksWithNonDeclaredTargets() extends AmfElementLinkResolutionStage {

  override def selector[T <: BaseUnit](l: Linkable, model: T): Boolean = {
    model match {
      case doc: Document =>
        val targetId = l.effectiveLinkTarget().id
        !doc.declares.exists(_.id == targetId)
      case _ => false
    }
  }
}
