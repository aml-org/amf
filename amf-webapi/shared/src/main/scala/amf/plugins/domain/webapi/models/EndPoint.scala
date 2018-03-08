package amf.plugins.domain.webapi.models

import amf.client.model.StrField
import amf.core.utils.Strings
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}

/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields, annotations: Annotations)
    extends DomainElement
    with ExtensibleWebApiDomainElement {

  def name: StrField                            = fields.field(Name)
  def description: StrField                     = fields.field(Description)
  def path: StrField                            = fields.field(Path)
  def operations: Seq[Operation]                = fields.field(Operations)
  def parameters: Seq[Parameter]                = fields.field(Parameters)
  def payloads: Seq[Payload]                    = fields(EndPointModel.Payloads)
  def security: Seq[ParametrizedSecurityScheme] = fields.field(Security)

  def parent: Option[EndPoint] = annotations.find(classOf[ParentEndPoint]).map(_.parent)

  def relativePath: String = parent.map(p => path.value().stripPrefix(p.path.value())).getOrElse(path.value())

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def resourceType: Option[ParametrizedResourceType] = extend collectFirst { case r: ParametrizedResourceType => r }

  def withName(name: String): this.type                                  = set(Name, name)
  def withDescription(description: String): this.type                    = set(Description, description)
  def withPath(path: String): this.type                                  = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type              = setArray(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type              = setArray(Parameters, parameters)
  def withSecurity(security: Seq[ParametrizedSecurityScheme]): this.type = setArray(Security, security)
  def withPayloads(payloads: Seq[Payload]): this.type                    = setArray(EndPointModel.Payloads, payloads)

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
    add(EndPointModel.Payloads, result)
    result
  }

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
    add(Security, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "/end-points/" + path.value().urlEncoded)

  override def meta = EndPointModel
}

object EndPoint {

  def apply(): EndPoint = apply(Annotations())

  def apply(annotations: Annotations): EndPoint = EndPoint(Fields(), annotations)
}
