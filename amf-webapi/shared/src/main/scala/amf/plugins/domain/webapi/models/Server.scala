package amf.plugins.domain.webapi.models

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.metamodel.ServerModel._
import org.yaml.model.YMap
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.models.bindings.ServerBindings
import amf.plugins.domain.webapi.models.security.SecurityRequirement

/**
  * Server internal model
  */
case class Server(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                     = fields.field(Name)
  def url: StrField                      = fields.field(Url)
  def description: StrField              = fields.field(Description)
  def variables: Seq[Parameter]          = fields.field(Variables)
  def protocol: StrField                 = fields.field(Protocol)
  def protocolVersion: StrField          = fields.field(ProtocolVersion)
  def security: Seq[SecurityRequirement] = fields.field(Security)
  def bindings: ServerBindings           = fields.field(Bindings)

  def withName(name: String): this.type                           = set(Name, name)
  def withUrl(url: String): this.type                             = set(Url, url)
  def withDescription(description: String): this.type             = set(Description, description)
  def withVariables(variables: Seq[Parameter]): this.type         = setArray(Variables, variables)
  def withProtocol(protocol: String): this.type                   = set(Protocol, protocol)
  def withProtocolVersion(protocolVersion: String): this.type     = set(ProtocolVersion, protocolVersion)
  def withSecurity(security: Seq[SecurityRequirement]): this.type = setArray(Security, security)
  def withBindings(bindings: ServerBindings): this.type           = set(Bindings, bindings)

  def withVariable(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Variables, result)
    result
  }

  def withSecurity(name: String): SecurityRequirement = {
    val result = SecurityRequirement().withName(name, Annotations() += SynthesizedField())
    add(Security, result)
    result
  }

  override def meta: DomainElementModel = ServerModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + url.option().orNull.urlComponentEncoded
}

object Server {

  def apply(): Server = apply(Annotations())

  def apply(ast: YMap): Server = apply(Annotations(ast))

  def apply(annotations: Annotations): Server = Server(Fields(), annotations)
}
