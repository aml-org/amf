package amf.apicontract.client.scala.model.domain.federation

import amf.apicontract.internal.metamodel.domain.federation.OperationFederationMetadataModel
import amf.apicontract.internal.metamodel.domain.federation.OperationFederationMetadataModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.federation.FederationMetadata
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.NodeShape
import org.yaml.model.YMap

case class OperationFederationMetadata(fields: Fields, annotations: Annotations) extends FederationMetadata {
  def providedEntity: NodeShape             = fields.field(ProvidedEntity)
  def federationMethod: StrField            = fields.field(FederationMethod)
  def keyMappings: Seq[ParameterKeyMapping] = fields.field(KeyMappings)

  def withProvidedEntity(providedEntity: NodeShape): this.type          = set(ProvidedEntity, providedEntity)
  def withFederationMethod(federationMethod: String): this.type         = set(FederationMethod, federationMethod)
  def withKeyMappings(keyMappings: Seq[ParameterKeyMapping]): this.type = setArray(KeyMappings, keyMappings)

  override def meta: OperationFederationMetadataModel.type = OperationFederationMetadataModel
  override def componentId                                 = s"/federation-metadata"
}

object OperationFederationMetadata {
  def apply(): OperationFederationMetadata                         = apply(Annotations())
  def apply(ast: YMap): OperationFederationMetadata                = apply(Annotations(ast))
  def apply(annotations: Annotations): OperationFederationMetadata = OperationFederationMetadata(Fields(), annotations)
}
