package amf.core.model.domain

import amf.core.metamodel.Obj
import amf.core.metamodel.domain.RecursiveShapeModel
import amf.core.metamodel.domain.RecursiveShapeModel._
import amf.core.model.StrField
import amf.core.parser.{Annotations, ErrorHandler, Fields}

case class RecursiveShape(override val fields: Fields, override val annotations: Annotations) extends Shape {

  def fixpoint: StrField = fields.field(FixPoint)

  def withFixPoint(shapeId: String): this.type = set(FixPoint, shapeId)

  override def cloneShape(recursionErrorHandler: Option[ErrorHandler],
                          recursionBase: Option[String],
                          traversed: IdsTraversionCheck = IdsTraversionCheck()): Shape = {
    val cloned = RecursiveShape()
    cloned.id = this.id
    copyFields(recursionErrorHandler, cloned, None, traversed)
    cloned
  }

  override def linkCopy(): Linkable = throw new Exception("Recursive shape cannot be linked")

  override def meta: Obj = RecursiveShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object RecursiveShape {
  def apply(): RecursiveShape =
    apply(Fields(), Annotations())

  def apply(annotations: Annotations): RecursiveShape =
    RecursiveShape(Fields(), annotations)

  def apply(l: Linkable): RecursiveShape =
    apply(Fields(), Annotations())
      .adopted(l.id)
      .withSupportsRecursion(l.supportsRecursion.value())
      .withFixPoint(l.id)

  def apply(shape: Shape): RecursiveShape =
    apply(Fields(), Annotations())
      .withName(shape.name.option().getOrElse("default-recursion"))
      .adopted(shape.id)
      .withSupportsRecursion(shape.supportsRecursion.value())
      .withFixPoint(shape.id)
}
