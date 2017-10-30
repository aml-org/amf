package amf.shape

import amf.domain.Annotation.{ExplicitField, ParsedFromTypeExpression}
import amf.domain.{CreativeWork, DomainElement, Example, Linkable}
import amf.metadata.shape.ShapeModel._
import amf.model.AmfArray

/**
  * Shape.
  */
abstract class Shape extends DomainElement with Linkable {

  def name: String                    = fields(Name)
  def displayName: String             = fields(DisplayName)
  def description: String             = fields(Description)
  def default: String                 = fields(Default)
  def values: Seq[String]             = fields(Values)
  def documentation: CreativeWork     = fields(Documentation)
  def xmlSerialization: XMLSerializer = fields(XMLSerialization)
  def inherits: Seq[Shape]            = fields(Inherits)
  def examples: Seq[Example]          = fields(Examples)

  def withName(name: String): this.type                                = set(Name, name)
  def withDisplayName(name: String): this.type                         = set(DisplayName, name)
  def withDescription(description: String): this.type                  = set(Description, description)
  def withDefault(default: String): this.type                          = set(Default, default)
  def withValues(values: Seq[String]): this.type                       = set(Values, values)
  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
  def withInherits(inherits: Seq[Shape]): this.type                    = setArray(Inherits, inherits)
  def withExamples(examples: Seq[Example]): this.type                  = setArray(Examples, examples)

  def fromTypeExpression: Boolean = this.annotations.contains(classOf[ParsedFromTypeExpression])
  def typeExpression: String = this.annotations.find(classOf[ParsedFromTypeExpression]) match {
    case Some(expr: ParsedFromTypeExpression) => expr.value
    case _                                    => throw new Exception("Trying to extract non existent type expression")
  }

  def cloneShape(): this.type = {
    val cloned = this match {
      case _: UnionShape    => UnionShape()
      case _: ScalarShape   => ScalarShape()
      case _: ArrayShape    => ArrayShape()
      case _: MatrixShape   => MatrixShape()
      case _: TupleShape    => TupleShape()
      case _: PropertyShape => PropertyShape()
      case _: FileShape     => FileShape()
      case _: AnyShape      => AnyShape()
      case _: NilShape      => NilShape()
      case _: NodeShape     => NodeShape()
    }
    cloned.id = this.id
    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape()
          case a: AmfArray =>
            AmfArray(a.values.map {
              case e: Shape => e.cloneShape()
              case o        => o
            }, a.annotations)
          case o => o
        }

        cloned.fields.setWithoutId(f, clonedValue, v.annotations)
    }
    if (cloned.isInstanceOf[NodeShape]) {
      cloned.add(ExplicitField())
    }
    cloned.asInstanceOf[this.type]
  }

}
