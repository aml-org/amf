package amf.apicontract.internal.spec.async.transformation

import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.spec.common.transformation.AsyncKeyCriteria
import amf.apicontract.internal.spec.common.transformation.stage.{AsyncKeyCriteria, JsonMergePatch}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.plugins.domain.apicontract.metamodel.OperationModel
import amf.plugins.domain.apicontract.resolution.stages.PayloadMediaTypeMerge

object AsyncJsonMergePatch {
  def apply(): JsonMergePatch =
    JsonMergePatch(_ => false,
                   AsyncKeyCriteria(),
                   commonIgnoredFields ++ operationIgnoredFields ++ messageIgnoredFields,
                   Set(CustomMessageExamplesMerge, PayloadMediaTypeMerge))

  private def operationIgnoredFields = Set(OperationModel.Method, OperationModel.Responses, OperationModel.Request)
  private def commonIgnoredFields    = Set(NameFieldSchema.Name, AbstractModel.IsAbstract, DomainElementModel.Extends)
  private def messageIgnoredFields   = Set(MessageModel.Payloads)
}
