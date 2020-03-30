package amf.plugins.domain.shapes.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.{LinkableElementModel, ShapeModel}
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.annotations.TypeAlias
import amf.plugins.domain.shapes.models.NodeShape

class TypeAliasTransformationStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: DeclaresModel => transform(doc)
      case _                  => // ignore
    }
    model.asInstanceOf[T]
  }

  private def transform(declaresModel: DeclaresModel): Unit = {
    val typeAliases: Seq[DomainElement] = collectTypeAliases(declaresModel)
    typeAliases.map { node =>
      addInherits(node)
      removeLink(node)
      node
    }
  }

  private def collectTypeAliases(declaresModel: DeclaresModel) = declaresModel.declares.filter {
    case shape: NodeShape => shape.annotations.contains(classOf[TypeAlias])
    case _                => false
  }

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
