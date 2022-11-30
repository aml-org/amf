package amf.apicontract.client.scala.model.domain.federation

import amf.apicontract.internal.metamodel.domain.federation.EndpointFederationMetadataModel
import amf.core.client.scala.model.domain.federation.FederationMetadata
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YMap

case class EndPointFederationMetadata(fields: Fields, annotations: Annotations) extends FederationMetadata {
  override def meta: EndpointFederationMetadataModel.type = EndpointFederationMetadataModel
  override def componentId                             = s"/federation-metadata"
}

object EndPointFederationMetadata {
  def apply(): EndPointFederationMetadata                         = apply(Annotations())
  def apply(ast: YMap): EndPointFederationMetadata                = apply(Annotations(ast))
  def apply(annotations: Annotations): EndPointFederationMetadata = EndPointFederationMetadata(Fields(), annotations)
}
