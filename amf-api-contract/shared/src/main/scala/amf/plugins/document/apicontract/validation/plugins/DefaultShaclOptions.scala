package amf.plugins.document.apicontract.validation.plugins

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DataNodeModel
import amf.core.internal.validation.core.ShaclValidationOptions

case class DefaultShaclOptions() extends ShaclValidationOptions {
  override val filterFields: Field => Boolean = (f: Field) => f.`type` == DataNodeModel
}
