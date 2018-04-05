package amf.core.model.domain

import amf.core.metamodel.Obj
import amf.core.metamodel.domain.RecursiveShapeModel
import amf.core.metamodel.domain.RecursiveShapeModel._
import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class RecursiveShape(override val fields: Fields, override val annotations: Annotations) extends Shape {

  def fixpoint: StrField = fields.field(FixPoint)

  def withFixPoint(shapeId: String): this.type = set(FixPoint, shapeId)

  override def cloneShape(recursionBase: Option[String], traversed: Set[String]): Shape = {
    val cloned = RecursiveShape()
    cloned.id = this.id
    copyFields(cloned, None, traversed)
    cloned
  }

  override def linkCopy(): Linkable = throw new Exception("Recursive shape cannot be linked")

  override def meta: Obj = RecursiveShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/recursion"
}

object RecursiveShape {
  def apply(): RecursiveShape = apply(Annotations())

  def apply(ast: YPart): RecursiveShape = apply(Annotations(ast))

  def apply(annotations: Annotations): RecursiveShape = RecursiveShape(Fields(), annotations)
}
