package amf.plugins.domain.shapes.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.{LinkableElementModel, ShapeModel}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.annotations.TypeAlias
import amf.plugins.domain.shapes.models.NodeShape

class TypeAliasTransformationStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = {
    model.transform(isTypeAlias, transform).asInstanceOf[T]
  }

  private def isTypeAlias(element: DomainElement): Boolean = element match {
    case shape: NodeShape => shape.annotations.contains(classOf[TypeAlias])
    case _                => false
  }

  private def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    addInherits(element)
    removeLink(element)
    removeTypeAlias(element)
    Some(element)
  }

  private def removeTypeAlias(element: DomainElement) = element.annotations.reject(_.isInstanceOf[TypeAlias])

  private def addInherits(element: DomainElement): Unit = element match {
    case linkable: Linkable =>
      val inheritsLink = NodeShape()
      linkable.linkTarget.foreach(linkTarget => inheritsLink.withLinkTarget(linkTarget))
      linkable.linkLabel.option().foreach(linkLabel => inheritsLink.withLinkLabel(linkLabel))
      element.setArray(ShapeModel.Inherits, Seq(inheritsLink))
    case _ => // ignore
  }

  private def removeLink(element: DomainElement): Unit = {
    element.fields.removeField(LinkableElementModel.Target)
    element.fields.removeField(LinkableElementModel.SupportsRecursion)
    element.fields.removeField(LinkableElementModel.TargetId)
    element.fields.removeField(LinkableElementModel.Label)
  }
}
