package amf.core.model.domain.extensions

import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel._
import amf.core.model.domain.{Shape, TraversedIds}
import amf.core.model.{BoolField, IntField, StrField}
import amf.core.parser.{Annotations, ErrorHandler, Fields}
import amf.core.utils.Strings

/**
  * Property shape
  */
case class PropertyShape(fields: Fields, annotations: Annotations) extends Shape {

  def path: StrField        = fields.field(Path)
  def range: Shape          = fields.field(Range)
  def minCount: IntField    = fields.field(MinCount)
  def maxCount: IntField    = fields.field(MaxCount)
  def readOnly: BoolField   = fields.field(ReadOnly)
  def writeOnly: BoolField  = fields.field(WriteOnly)
  def deprecated: BoolField = fields.field(Deprecated)
  def patternName: StrField     = fields.field(PatternName)

  def withPath(path: String): this.type  = set(Path, path)
  def withRange(range: Shape): this.type = set(Range, range)

  def withMinCount(min: Int): this.type              = set(MinCount, min)
  def withMaxCount(max: Int): this.type              = set(MaxCount, max)
  def withReadOnly(readOnly: Boolean): this.type     = set(ReadOnly, readOnly)
  def withWriteOnly(writeOnly: Boolean): this.type   = set(WriteOnly, writeOnly)
  def withDeprecated(deprecated: Boolean): this.type = set(Deprecated, deprecated)
  def withPatternName(pattern: String): this.type    = set(PatternName, pattern)

  override def adopted(parent: String): this.type = {
    simpleAdoption(parent)
    if (Option(range).isDefined) {
      range.adopted(id)
    }
    this
  }

  override def linkCopy(): PropertyShape = PropertyShape().withId(id)

  override def meta: ShapeModel = PropertyShapeModel

  override def cloneShape(recursionErrorHandler: Option[ErrorHandler],
                          withRecursionBase: Option[String],
                          traversed: TraversedIds): PropertyShape = {
    val cloned = PropertyShape(Annotations(annotations))
    cloned.id = this.id
    copyFields(recursionErrorHandler, cloned, withRecursionBase, traversed)
    cloned.asInstanceOf[this.type]
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/property/" + name.option().getOrElse("default-property").urlComponentEncoded
}

object PropertyShape {
  def apply(): PropertyShape = apply(Annotations())

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
