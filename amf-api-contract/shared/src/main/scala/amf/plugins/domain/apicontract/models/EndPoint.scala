package amf.plugins.domain.apicontract.models

import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.NamedDomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.apicontract.annotations.ParentEndPoint
import amf.plugins.domain.apicontract.metamodel.EndPointModel
import amf.plugins.domain.apicontract.metamodel.EndPointModel._
import amf.plugins.domain.apicontract.models.bindings.ChannelBindings
import amf.plugins.domain.apicontract.models.templates.{ParametrizedResourceType, ParametrizedTrait}

/**
  * EndPoint internal model
  */
class EndPoint(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with SecuredElement
    with ExtensibleWebApiDomainElement
    with ServerContainer {

  def description: StrField      = fields.field(Description)
  def summary: StrField          = fields.field(Summary)
  def path: StrField             = fields.field(Path)
  def operations: Seq[Operation] = fields.field(Operations)
  def parameters: Seq[Parameter] = fields.field(Parameters)
  def payloads: Seq[Payload]     = fields.field(Payloads)
  def servers: Seq[Server]       = fields.field(Servers)
  def bindings: ChannelBindings  = fields.field(Bindings)

  def parent: Option[EndPoint] = annotations.find(classOf[ParentEndPoint]).flatMap(_.parent)

  def relativePath: String = parent.map(p => path.value().stripPrefix(p.path.value())).getOrElse(path.value())

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def resourceType: Option[ParametrizedResourceType] = extend collectFirst { case r: ParametrizedResourceType => r }

  def withDescription(description: String): this.type       = set(Description, description)
  def withSummary(summary: String): this.type               = set(Summary, summary)
  def withPath(path: String): this.type                     = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type = setArray(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type = setArray(Parameters, parameters)
  def withPayloads(payloads: Seq[Payload]): this.type       = setArray(Payloads, payloads)
  def withServers(servers: Seq[Server]): this.type          = setArray(Servers, servers)
  def withBindings(bindings: ChannelBindings): this.type    = set(Bindings, bindings)

  override def removeServers(): Unit = fields.removeField(EndPointModel.Servers)

  def withOperation(method: String): Operation = {
    val result = Operation().withMethod(method)
    add(Operations, result)
    result
  }

  def withParameter(name: String): Parameter = {
    val result = Parameter().withName(name).withParameterName(name)
    add(Parameters, result)
    result
  }

  def withPayload(mediaType: Option[String] = None): Payload = {
    val result = Payload()
    mediaType.foreach(result.withMediaType)
    add(Payloads, result)
    result
  }

  def withServer(url: String): Server = {
    val result = Server().withUrl(url)
    add(Servers, result)
    result
  }

  override def meta: EndPointModel.type = EndPointModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/end-points/" + path.value().urlComponentEncoded
  override def nameField: Field    = Name
}

object EndPoint {

  def apply(): EndPoint = apply(Annotations())

  def apply(annotations: Annotations): EndPoint = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): EndPoint = new EndPoint(fields, annotations)
}
