package amf.plugins.domain.webapi.models.security

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel._
import amf.core.utils.Strings
import org.yaml.model.YPart

case class OAuth2Flow(fields: Fields, annotations: Annotations) extends DomainElement {
  def authorizationUri: StrField = fields.field(AuthorizationUri)
  def accessTokenUri: StrField   = fields.field(AccessTokenUri)
  def refreshUri: StrField       = fields.field(RefreshUri)
  def scopes: Seq[Scope]         = fields.field(Scopes)
  def flow: StrField             = fields.field(Flow)

  def withAuthorizationUri(authorizationUri: String): this.type = set(AuthorizationUri, authorizationUri)
  def withAccessTokenUri(accessTokenUri: String): this.type     = set(AccessTokenUri, accessTokenUri)
  def withRefreshUri(refreshUri: String): this.type             = set(RefreshUri, refreshUri)
  def withScopes(scopes: Seq[Scope]): this.type                 = setArray(Scopes, scopes)
  def withFlow(flow: String): this.type                         = set(Flow, flow)

  override def meta: Obj = OAuth2FlowModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + flow.option().getOrElse("default-flow").urlComponentEncoded
}

object OAuth2Flow {

  def apply(): OAuth2Flow = apply(Annotations())

  def apply(part: YPart): OAuth2Flow = apply(Annotations(part))

  def apply(annotations: Annotations): OAuth2Flow = new OAuth2Flow(Fields(), annotations)
}
