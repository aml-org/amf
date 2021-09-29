package amf.apicontract.client.scala.model.document

import amf.aml.internal.metamodel.document.DialectInstanceProcessingDataModel.SourceSpec
import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel
import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.BaseUnitProcessingData
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.remote.Spec
import amf.apicontract.internal.unsafe.ApiContractBuildInfo

case class APIContractProcessingData(override val fields: Fields, override val annotations: Annotations)
    extends BaseUnitProcessingData(fields, annotations) {
  def modelVersion: StrField = fields.field(APIContractModelVersion)

  override protected[amf] def sourceSpecProvider: Option[Spec] = sourceSpec.option().map(Spec.apply)

  set(APIContractModelVersion, ApiContractBuildInfo.apiContractModelVersion)

  override def withTransformed(value: Boolean): APIContractProcessingData.this.type = super.withTransformed(value)
  override def meta: APIContractProcessingDataModel.type                            = APIContractProcessingDataModel
}

object APIContractProcessingData {
  def apply(): APIContractProcessingData = apply(Annotations())
  def apply(annotations: Annotations): APIContractProcessingData =
    APIContractProcessingData(Fields(), annotations)
}
