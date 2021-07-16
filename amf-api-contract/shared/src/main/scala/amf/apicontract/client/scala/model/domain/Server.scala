package amf.apicontract.client.scala.model.domain

import amf.apicontract.client.scala.model.domain.bindings.ServerBindings
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.metamodel.domain.ServerModel._
import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import org.yaml.model.YMap

/**
  * Server internal model
  */
case class Server(fields: Fields, annotations: Annotations) extends SecuredElement {

  def name: StrField            = fields.field(Name)
  def url: StrField             = fields.field(Url)
  def description: StrField     = fields.field(Description)
  def variables: Seq[Parameter] = fields.field(Variables)
  def protocol: StrField        = fields.field(Protocol)
  def protocolVersion: StrField = fields.field(ProtocolVersion)
  def bindings: ServerBindings  = fields.field(Bindings)

  def withName(name: String): this.type                       = set(Name, name)
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
  private[amf] override def componentId: String = "/" + url.option().orNull.urlComponentEncoded
}

object Server {

  def apply(): Server = apply(Annotations())

  def apply(ast: YMap): Server = apply(Annotations(ast))

  def apply(annotations: Annotations): Server = Server(Fields(), annotations)
}
