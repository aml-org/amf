package amf.apicontract.client.scala.model.domain

import amf.apicontract.client.scala.model.domain.bindings.ServerBindings
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.metamodel.domain.ServerModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import org.yaml.model.YMap

/** Server internal model
  */
class Server(override val fields: Fields, override val annotations: Annotations)
    extends SecuredElement
    with NamedDomainElement
    with Linkable {

  override protected def nameField: Field = Name

  def url: StrField             = fields.field(Url)
  def description: StrField     = fields.field(Description)
  def variables: Seq[Parameter] = fields.field(Variables)
  def protocol: StrField        = fields.field(Protocol)
  def protocolVersion: StrField = fields.field(ProtocolVersion)
  def bindings: ServerBindings  = fields.field(Bindings)

  def withUrl(url: String): this.type                         = set(Url, url)
  def withDescription(description: String): this.type         = set(Description, description)
  def withVariables(variables: Seq[Parameter]): this.type     = setArray(Variables, variables)
  def withProtocol(protocol: String): this.type               = set(Protocol, protocol)
  def withProtocolVersion(protocolVersion: String): this.type = set(ProtocolVersion, protocolVersion)
  def withBindings(bindings: ServerBindings): this.type       = set(Bindings, bindings)

  def withVariable(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Variables, result)
    result
  }

  override def meta: DomainElementModel = ServerModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + url.option().orNull.urlComponentEncoded

  override def linkCopy(): Server = Server().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Server.apply

}

object Server {

  def apply(): Server = apply(Annotations())

  def apply(ast: YMap): Server = apply(Annotations(ast))

  def apply(annotations: Annotations): Server = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Server = new Server(fields, annotations)
}
