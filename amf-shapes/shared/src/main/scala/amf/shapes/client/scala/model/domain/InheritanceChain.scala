package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.DeclaredElement

import scala.collection.mutable

// Support to track during resolution the
// inheritance chain between shapes as loosely
// defined in RAML
trait InheritanceChain { this: AnyShape =>
  // Array of subtypes to compute
  var subTypes: mutable.Seq[Shape]   = mutable.Seq()
  var superTypes: mutable.Seq[Shape] = mutable.Seq()
  // Any ID, not only the ones with discriminator, just keep the ID ref
  var inheritedIds: mutable.Seq[String] = mutable.Seq()

  def addSubType(shape: Shape): Unit = {
    subTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => subTypes ++= Seq(shape)
    }
  }

  def addSuperType(shape: Shape): Unit = {
    superTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => superTypes ++= Seq(shape)
    }
  }

  def linkSubType(shape: AnyShape): Unit = {
    addSubType(shape)
    shape.addSuperType(this)
  }

  def effectiveStructuralShapes: Seq[Shape] = {
    val acc =
      if (annotations
            .contains(classOf[DeclaredElement])) { // problem with inlined types extending types with discriminator
        computeSubtypesClosure()
      } else {
        superTypes.find(_.isInstanceOf[AnyShape]) match { // which one if multiple inheritance?
          case Some(superType: AnyShape) => superType.effectiveStructuralShapes
          case _                         => Nil
        }
      }
    (Seq(this) ++ acc).distinct
  }

  protected def computeSubtypesClosure(): Seq[Shape] = {
    val res =
      if (subTypes.isEmpty) Nil
      else
        subTypes.foldLeft(Seq[Shape]()) {
          case (acc, nextShape) =>
            nextShape match {
              case nestedNode: NodeShape => acc ++ Seq(nestedNode) ++ nestedNode.computeSubtypesClosure
              case _                     => acc
            }
        }
    res.distinct
  }
}
