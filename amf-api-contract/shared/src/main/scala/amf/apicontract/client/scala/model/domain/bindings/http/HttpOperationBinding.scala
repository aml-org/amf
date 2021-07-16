package amf.apicontract.client.scala.model.domain.bindings.http
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.HttpOperationBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.HttpOperationBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.shapes.client.scala.model.domain.Key

class HttpOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {

  override def key: StrField = fields.field(HttpOperationBindingModel.key)

  def method: StrField        = fields.field(Method)
  def query: Shape            = fields.field(Query)
  def operationType: StrField = fields.field(OperationType)

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: HttpOperationBindingModel.type = HttpOperationBindingModel

  def withOperationType(`type`: String): this.type = set(OperationType, `type`)
  def withMethod(method: String): this.type        = set(Method, method)
  def withQuery(query: Shape): this.type           = set(Query, query)

  private[amf] override def componentId: String = "/http-operation"

  override def linkCopy(): HttpOperationBinding = HttpOperationBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpOperationBinding.apply
}

object HttpOperationBinding {

  def apply(): HttpOperationBinding = apply(Annotations())

  def apply(annotations: Annotations): HttpOperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpOperationBinding =
    new HttpOperationBinding(fields, annotations)
}
