package amf.plugins.domain.shapes.resolution.stages.merge

import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.plugins.domain.apicontract.metamodel.{AbstractModel, MessageModel, OperationModel}
import amf.plugins.domain.apicontract.resolution.stages.{CustomMessageExamplesMerge, PayloadMediaTypeMerge}

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
