package amf.plugins.document.apicontract.validation.plugins

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DataNodeModel
import amf.core.services.ShaclValidationOptions

case class DefaultShaclOptions() extends ShaclValidationOptions {
  override val filterFields: Field => Boolean = (f: Field) => f.`type` == DataNodeModel
}
