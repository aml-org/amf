package amf.shapes.client.scala.model.domain.core

import amf.core.client.scala.model.BoolField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel.Name
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.ExemplifiedDomainElement
import amf.shapes.internal.domain.metamodel.core.ShapeParameterModel.Required
import amf.shapes.internal.domain.metamodel.core.ShapeRequestModel
import amf.shapes.internal.domain.metamodel.core.ShapeRequestModel.QueryParameters

private[amf] class ShapeRequest(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with ExemplifiedDomainElement
    with Linkable {

  def required: BoolField                  = fields.field(Required)
  def queryParameters: Seq[ShapeParameter] = fields.field(QueryParameters)

  def withRequired(required: Boolean): this.type                      = set(Required, required)
  def withQueryParameters(parameters: Seq[ShapeParameter]): this.type = setArray(QueryParameters, parameters)

  def withQueryParameter(name: String): ShapeParameter = {
    val result = ShapeParameter().withName(name)
    add(QueryParameters, result)
    result
  }

  override def meta: ShapeRequestModel.type = ShapeRequestModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/" + name.option().getOrElse("request").urlComponentEncoded

  override def linkCopy(): ShapeRequest = ShapeRequest().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new ShapeRequest(fields, annot)

  override protected def nameField: Field = Name
}

object ShapeRequest {
  def apply() = new ShapeRequest(Fields(), Annotations())
}
