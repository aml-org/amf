package amf.apicontract.client.scala.model.document

import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel
import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.BaseUnitProcessingData
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.remote.Spec

case class APIContractProcessingData(override val fields: Fields, override val annotations: Annotations)
    extends BaseUnitProcessingData(fields, annotations) {
  def modelVersion: StrField = fields.field(APIContractModelVersion)

  def sourceSpec: StrField = fields.field(SourceSpec)

  override protected[amf] def sourceSpecProvider: Option[Spec] = sourceSpec.option().map(Spec.apply)

  def withSourceSpec(spec: String): this.type = set(SourceSpec, Spec(spec).id)

  def withSourceSpec(spec: Spec): this.type = set(SourceSpec, spec.id)

  set(APIContractModelVersion, "3.1.0") // TODO set this value with SBT

  override def withTransformed(value: Boolean): APIContractProcessingData.this.type = super.withTransformed(value)
  override def meta: APIContractProcessingDataModel.type                            = APIContractProcessingDataModel
}

object APIContractProcessingData {
  def apply(): APIContractProcessingData = apply(Annotations())
  def apply(annotations: Annotations): APIContractProcessingData =
    APIContractProcessingData(Fields(), annotations)
}
