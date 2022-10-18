package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.RequestModel
import amf.apicontract.internal.metamodel.domain.RequestModel._
import amf.core.client.scala.model.BoolField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.operations.AbstractRequest
import org.yaml.model.YPart

/** Request internal model.
  */
class Request(override val fields: Fields, override val annotations: Annotations)
    extends Message(fields: Fields, annotations: Annotations)
    with AbstractRequest {

  override type ParameterType = Parameter

  override private[amf] def buildQueryParameter = Parameter()

  def required: BoolField                      = fields.field(Required)
  override def queryParameters: Seq[Parameter] = fields.field(QueryParameters)
  def queryString: Shape                       = fields.field(QueryString)
  def uriParameters: Seq[Parameter]            = fields.field(UriParameters)
  def cookieParameters: Seq[Parameter]         = fields.field(CookieParameters)

  def withRequired(required: Boolean): this.type                          = set(Required, required)
  override def withQueryParameters(parameters: Seq[Parameter]): this.type = setArray(QueryParameters, parameters)
  def withQueryString(queryString: Shape): this.type                      = set(QueryString, queryString)
  def withUriParameters(uriParameters: Seq[Parameter]): this.type         = setArray(UriParameters, uriParameters)
  def withCookieParameters(cookieParameters: Seq[Parameter]): this.type   = setArray(CookieParameters, cookieParameters)

  override def withQueryParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(QueryParameters, result)
    result
  }

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withUriParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(UriParameters, result)
    result
  }

  def withCookieParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(CookieParameters, result)
    result
  }

  override def meta: RequestModel.type = RequestModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/request" + name.option().map("/" + _).getOrElse("").urlComponentEncoded

  override def linkCopy(): Request = Request().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new Request(fields, annot)
}

object Request {

  def apply(): Request                                         = apply(Annotations())
  def apply(ast: YPart): Request                               = apply(Annotations(ast))
  def apply(annotations: Annotations): Request                 = new Request(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): Request = new Request(fields, annotations)
}
