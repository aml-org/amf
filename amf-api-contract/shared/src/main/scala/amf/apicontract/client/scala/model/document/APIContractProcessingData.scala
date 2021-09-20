package amf.apicontract.client.scala.model.document

import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel
import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.BaseUnitProcessingData
import amf.core.internal.parser.domain.{Annotations, Fields}

case class APIContractProcessingData(override val fields: Fields, override val annotations: Annotations)
    extends BaseUnitProcessingData(fields, annotations) {
  def modelVersion: StrField = fields.field(APIContractModelVersion)

  set(APIContractModelVersion, "3.1.0") // TODO set this value with SBT

  override def meta: APIContractProcessingDataModel.type = APIContractProcessingDataModel
}

object APIContractProcessingData {
  def apply(): APIContractProcessingData = apply(Annotations())
  def apply(annotations: Annotations): APIContractProcessingData =
    APIContractProcessingData(Fields(), annotations)
}
