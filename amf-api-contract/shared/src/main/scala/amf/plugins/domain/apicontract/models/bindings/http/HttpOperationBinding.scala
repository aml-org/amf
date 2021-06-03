package amf.plugins.domain.apicontract.models.bindings.http
import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.HttpOperationBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.HttpOperationBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, OperationBinding}

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

  override def componentId: String = "/http-operation"

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
