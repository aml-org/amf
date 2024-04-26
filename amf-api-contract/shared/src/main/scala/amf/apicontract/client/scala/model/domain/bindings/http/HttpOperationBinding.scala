package amf.apicontract.client.scala.model.domain.bindings.http
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{HttpOperationBinding010Model, HttpOperationBindingModel}
import amf.apicontract.internal.metamodel.domain.bindings.HttpOperationBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.shapes.client.scala.model.domain.Key

abstract class HttpOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {
  override def key: StrField                        = fields.field(HttpOperationBindingModel.key)
  override protected def bindingVersionField: Field = BindingVersion
  override def componentId: String                  = "/http-operation"

  def method: StrField = fields.field(Method)
  def query: Shape     = fields.field(Query)

  def withMethod(method: String): this.type = set(Method, method)
  def withQuery(query: Shape): this.type    = set(Query, query)
}

class HttpOperationBinding010(override val fields: Fields, override val annotations: Annotations)
    extends HttpOperationBinding(fields, annotations) {
  override def componentId: String = "/http-operation-010"

  def operationType: StrField                      = fields.field(HttpOperationBinding010Model.OperationType)
  def withOperationType(`type`: String): this.type = set(HttpOperationBinding010Model.OperationType, `type`)

  override def meta: HttpOperationBinding010Model.type = HttpOperationBinding010Model

  override def linkCopy(): HttpOperationBinding010 = HttpOperationBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpOperationBinding010.apply
}

object HttpOperationBinding010 {
  def apply(): HttpOperationBinding010 = apply(Annotations())

  def apply(annotations: Annotations): HttpOperationBinding010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpOperationBinding010 =
    new HttpOperationBinding010(fields, annotations)
}
