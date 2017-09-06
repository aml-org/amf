package amf.model

case class PropertyShape(private[amf] val propertyShape: amf.shape.PropertyShape) {

  val path: String = propertyShape.path
  val range: Shape = wrapShape(propertyShape.range)

  val minCount: Int = propertyShape.minCount
  val maxCount: Int = propertyShape.maxCount

  def withPath(path: String): this.type = {
    propertyShape.withPath(path)
    this
  }

  def withRange(range: Shape): this.type = {
    propertyShape.withRange(range.shape)
    this
  }

  def withMinCount(min: Int): this.type = {
    propertyShape.withMinCount(min)
    this
  }
  def withMaxCount(max: Int): this.type = {
    propertyShape.withMaxCount(max)
    this
  }

  def withObjectRange(name: String): NodeShape = {
    NodeShape(propertyShape.withObjectRange(name))
  }

  def withScalarSchema(name: String): ScalarShape = {
    ScalarShape(propertyShape.withScalarSchema(name))
  }

  private def wrapShape(shape: amf.shape.Shape): Shape =
    (shape match {
      case node: amf.shape.NodeShape     => Some(NodeShape(node))
      case scalar: amf.shape.ScalarShape => Some(ScalarShape(scalar))
      case a                             => None
    }).orNull

}
