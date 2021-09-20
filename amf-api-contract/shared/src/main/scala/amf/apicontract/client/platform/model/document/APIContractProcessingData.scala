package amf.apicontract.client.platform.model.document

import amf.apicontract.client.scala.model.document.{APIContractProcessingData => InternalAPIContractProcessingData}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.document.BaseUnitProcessingData
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class APIContractProcessingData(override private[amf] val _internal: InternalAPIContractProcessingData)
    extends BaseUnitProcessingData {

  @JSExportTopLevel("APIContractProcessingData")
  def this() = this(InternalAPIContractProcessingData())

  def modelVersion: StrField = _internal.modelVersion
}
