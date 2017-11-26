package amf.plugins.domain.shapes.models

trait ShapeHelpers { this: Shape =>

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
    copyFields(cloned)
    if (cloned.isInstanceOf[NodeShape]) {
      cloned.add(ExplicitField())
    }
    cloned.asInstanceOf[this.type]
  }
}