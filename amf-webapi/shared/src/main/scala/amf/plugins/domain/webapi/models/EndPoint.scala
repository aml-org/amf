package amf.plugins.domain.webapi.models

import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}


/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields, annotations: Annotations) extends DomainElement with ExtensibleWebApiDomainElement {

  def name: String                              = fields(Name)
  def description: String                       = fields(Description)
  def path: String                              = fields(Path)
  def operations: Seq[Operation]                = fields(Operations)
  def parameters: Seq[Parameter]                = fields(UriParameters)
  def security: Seq[ParametrizedSecurityScheme] = fields(Security)

  def parent: Option[EndPoint] = annotations.find(classOf[ParentEndPoint]).map(_.parent)

  def relativePath: String = parent.map(p => path.stripPrefix(p.path)).getOrElse(path)

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def resourceType: Option[ParametrizedResourceType] = extend collectFirst { case r: ParametrizedResourceType => r }

  def withName(name: String): this.type                                  = set(Name, name)
  def withDescription(description: String): this.type                    = set(Description, description)
  def withPath(path: String): this.type                                  = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type              = setArray(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type              = setArray(UriParameters, parameters)
  def withSecurity(security: Seq[ParametrizedSecurityScheme]): this.type = setArray(Security, security)

  def withOperation(method: String): Operation = {
    val result = Operation().withMethod(method)
    add(Operations, result)
    result
  }

  def withParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(UriParameters, result)
    result
  }

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
    add(Security, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "/end-points/" + path.urlEncoded)

  override def meta = EndPointModel
}

object EndPoint {

  def apply(): EndPoint = apply(Annotations())

  def apply(annotations: Annotations): EndPoint = EndPoint(Fields(), annotations)
}
