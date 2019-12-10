package amf.plugins.domain.webapi.models.bindings.http
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{Shape, Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.models.bindings.{OperationBinding, BindingVersion}
import amf.plugins.domain.webapi.metamodel.bindings.HttpOperationBindingModel
import amf.plugins.domain.webapi.metamodel.bindings.HttpOperationBindingModel._

class HttpOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion {
  def `type`: StrField = fields.field(Type)
  def method: StrField = fields.field(Method)
  def query: Shape     = fields.field(Query)

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = HttpOperationBindingModel

  def withType(`type`: String): this.type   = set(Type, `type`)
  def withMethod(method: String): this.type = set(Method, method)
  def withQuery(query: Shape): this.type    = set(Query, query)

  override def componentId: String = "/http-operation"

  override def linkCopy(): HttpOperationBinding = HttpOperationBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = HttpOperationBinding.apply
}

object HttpOperationBinding {

  def apply(): HttpOperationBinding = apply(Annotations())

  def apply(annotations: Annotations): HttpOperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpOperationBinding =
    new HttpOperationBinding(fields, annotations)
}
