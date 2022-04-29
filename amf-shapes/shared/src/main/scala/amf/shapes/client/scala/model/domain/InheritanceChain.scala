package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.DeclaredElement

import scala.collection.mutable

// Support to track during resolution the
// inheritance chain between shapes as loosely
// defined in RAML
trait InheritanceChain { this: AnyShape =>
  // Array of subtypes to compute
  private var subTypes: mutable.Seq[Shape]   = mutable.Seq()
  private var superTypes: mutable.Seq[Shape] = mutable.Seq()
  // Any ID, not only the ones with discriminator, just keep the ID ref
  private[amf] var inheritedIds: mutable.Seq[String] = mutable.Seq()

  protected[amf] def addSubType(shape: Shape): Unit = {
    subTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => subTypes ++= Seq(shape)
    }
  }

  protected[amf] def addSuperType(shape: Shape): Unit = {
    superTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => superTypes ++= Seq(shape)
    }
  }

  protected[amf] def linkSubType(shape: AnyShape): Unit = {
    addSubType(shape)
    shape.addSuperType(this)
  }

  protected def computeSubtypesClosure(): Seq[Shape] = {
    val res =
      if (subTypes.isEmpty) Nil
      else
        subTypes.foldLeft(Seq[Shape]()) { case (acc, nextShape) =>
          nextShape match {
            case nestedNode: NodeShape => acc ++ Seq(nestedNode) ++ nestedNode.computeSubtypesClosure
            case _                     => acc
          }
        }
    res.distinct
  }
}
