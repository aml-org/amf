package amf.plugins.domain.shapes.models

import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel._
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ScalarShapeModel}
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def dataType: StrField = fields.field(DataType)

  def withDataType(dataType: String): this.type = set(DataType, dataType)

  override def linkCopy(): ScalarShape = ScalarShape().withId(id)

  override def meta: AnyShapeModel = ScalarShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/scalar/" + name.option().getOrElse("default-scalar").urlComponentEncoded

  override def copyShape(): ScalarShape = ScalarShape(fields.copy(), annotations.copy()).withId(id)

  override def ramlSyntaxKey: String = dataType.option().getOrElse("#shape").split("#").last match {
    case "integer" | "float" | "double" | "long" | "number" => "numberScalarShape"
    case "string"                                           => "stringScalarShape"
    case "dateTime"                                         => "dateScalarShape"
    case _                                                  => "shape"
  }

}

object ScalarShape {
  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}