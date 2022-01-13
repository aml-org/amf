package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeRequestModel
import org.yaml.model.YPart

case class ShapeRequest(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  def queryParameters: Seq[ShapeParameter]  = fields.field(ShapeRequestModel.QueryParameters)

  def withQueryParameters(parameters: Seq[ShapeParameter]): this.type = setArray(ShapeRequestModel.QueryParameters, parameters)

  def withQueryParameter(name: String): ShapeParameter = {
    val result = ShapeParameter().withName(name)
    add(ShapeRequestModel.QueryParameters, result)
    result
  }

  def meta: ShapeRequestModel.type = ShapeRequestModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] def componentId: String = "/request"

  override def nameField: Field = ShapeRequestModel.Name
}


object ShapeRequest {
  def apply(): ShapeRequest                                         = apply(Annotations())
  def apply(ast: YPart): ShapeRequest                               = apply(Annotations(ast))
  def apply(annotations: Annotations): ShapeRequest                 = new ShapeRequest(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): ShapeRequest = new ShapeRequest(fields, annotations)
}

