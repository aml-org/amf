package amf.apicontract.internal.transformation.stages

import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.RecursiveInheritance
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.RecursiveShape
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.domain.ShapeModel.Inherits
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.NodeShape

case class GraphQLInterfaceRecursionDetectionStage() extends TransformationStep() with PlatformSecrets {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    implicit val eh: AMFErrorHandler = errorHandler
    model match {
      case d: DeclaresModel =>
        d.declares.foreach {
          case node: NodeShape if isInterface(node) => traverse(node) // only interfaces might have cyclic inheritance
          case _                                    =>                // ignore
        }
      case _ => // ignore
    }
    model
  }

  private def isInterface(node: NodeShape): Boolean = node.isAbstract.value()

  def traverse(current: NodeShape, previous: Seq[NodeShape] = Nil)(implicit eh: AMFErrorHandler): Unit = {
    // Place recursive shapes in current level
    val newInherits = current.inherits.map {
      case next: NodeShape if isInterface(next) && previous.contains(next) =>
        reportError(current, previous, next, eh)
        val rs = RecursiveShape(next)
        rs.adopted(current.id)
        rs
      case next => next
    }
    if (newInherits.nonEmpty) current.setArrayWithoutId(Inherits, newInherits)

    // Traverse next level
    current.inherits.foreach {
      case next: NodeShape if isInterface(next) => traverse(next, previous :+ current)
      case _                                    => // skip
    }
  }

  private def reportError(current: NodeShape, previous: Seq[NodeShape], next: NodeShape, eh: AMFErrorHandler): Unit = {
    val message = {
      val chain = (previous :+ current :+ next).map(_.name.value()).mkString(" -> ")
      s"Invalid cyclic interface implementations $chain"
    }
    eh.violation(RecursiveInheritance, current, message, current.annotations)
  }
}
