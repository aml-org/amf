package amf.plugins.document.webapi.validation.runner

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DataNodeModel
import amf.core.services.ValidationOptions

case class FilterDataNodeOptions() extends ValidationOptions {
  override val filterFields: Field => Boolean = (f: Field) => f.`type` == DataNodeModel
}
