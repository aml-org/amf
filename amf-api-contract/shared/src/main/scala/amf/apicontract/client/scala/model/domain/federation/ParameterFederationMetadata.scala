package amf.apicontract.client.scala.model.domain.federation

import amf.apicontract.internal.metamodel.domain.federation.ParameterFederationMetadataModel
import amf.core.client.scala.model.domain.federation.FederationMetadata
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YMap

case class ParameterFederationMetadata(fields: Fields, annotations: Annotations) extends FederationMetadata {
  override def meta: ParameterFederationMetadataModel.type = ParameterFederationMetadataModel
  override def componentId                                 = s"/federation-metadata"
}

object ParameterFederationMetadata {
  def apply(): ParameterFederationMetadata                         = apply(Annotations())
  def apply(ast: YMap): ParameterFederationMetadata                = apply(Annotations(ast))
  def apply(annotations: Annotations): ParameterFederationMetadata = ParameterFederationMetadata(Fields(), annotations)
}
