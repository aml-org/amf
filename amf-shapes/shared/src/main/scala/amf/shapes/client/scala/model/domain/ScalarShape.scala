package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.ScalarShapeModel._
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ScalarShapeModel}
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def dataType: StrField  = fields.field(DataType)
  def encoding: StrField  = fields.field(Encoding)
  def mediaType: StrField = fields.field(MediaType)
  def schema: Shape       = fields.field(Schema)

  def withDataType(dataType: String, annotations: Annotations = Annotations()): this.type =
    set(DataType, dataType, annotations)
  def withEncoding(encoding: String): this.type   = set(Encoding, encoding)
  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)
  def withSchema(schema: Shape): this.type        = set(Schema, schema)

  override def linkCopy(): ScalarShape = ScalarShape().withId(id)

  override val meta: ScalarShapeModel.type = ScalarShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/scalar/" + name.option().getOrElse("default-scalar").urlComponentEncoded

  override def ramlSyntaxKey: String = dataType.option().getOrElse("#shape").split("#").last match {
    case "integer" | "float" | "double" | "long" | "number" => "numberScalarShape"
    case "string"                                           => "stringScalarShape"
    case "dateTime"                                         => "dateScalarShape"
    case _                                                  => "shape"
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ScalarShape.apply
}

object ScalarShape {
  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}
