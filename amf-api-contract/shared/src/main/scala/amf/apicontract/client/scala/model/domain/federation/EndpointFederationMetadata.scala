package amf.apicontract.client.scala.model.domain.federation

import amf.apicontract.internal.metamodel.domain.federation.EndpointFederationMetadataModel
import amf.core.client.scala.model.domain.federation.FederationMetadata
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YMap

case class EndpointFederationMetadata(fields: Fields, annotations: Annotations) extends FederationMetadata {
  override def meta: EndpointFederationMetadataModel.type = EndpointFederationMetadataModel
  override def componentId                                = s"/federation-metadata"
}

object EndpointFederationMetadata {
  def apply(): EndpointFederationMetadata                         = apply(Annotations())
  def apply(ast: YMap): EndpointFederationMetadata                = apply(Annotations(ast))
  def apply(annotations: Annotations): EndpointFederationMetadata = EndpointFederationMetadata(Fields(), annotations)
}
