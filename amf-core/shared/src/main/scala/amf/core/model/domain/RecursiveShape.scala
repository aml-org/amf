package amf.core.model.domain

import amf.core.metamodel.Obj
import amf.core.metamodel.domain.RecursiveShapeModel
import amf.core.metamodel.domain.RecursiveShapeModel._
import amf.core.model.StrField
import amf.core.parser.{Annotations, ErrorHandler, Fields}

case class RecursiveShape(private val fixName: String,
                          override val fields: Fields,
                          override val annotations: Annotations)
    extends Shape {

  set(Name, fixName)

  def fixpoint: StrField = fields.field(FixPoint)

  def withFixPoint(shapeId: String): this.type = set(FixPoint, shapeId)

  override def cloneShape(recursionErrorHandler: Option[ErrorHandler],
                          recursionBase: Option[String],
                          traversed: TraversedIds = TraversedIds()): Shape = {
    val cloned = RecursiveShape(fixName)
    cloned.id = this.id
    copyFields(recursionErrorHandler, cloned, None, traversed)
    cloned
  }

  override def linkCopy(): Linkable = throw new Exception("Recursive shape cannot be linked")

  override def meta: Obj = RecursiveShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/recursion"
}

object RecursiveShape {
  def apply(fixName: String): RecursiveShape = apply(fixName, Fields(), Annotations())

  def apply(fixName: String, annotations: Annotations): RecursiveShape = RecursiveShape(fixName, Fields(), annotations)
}
