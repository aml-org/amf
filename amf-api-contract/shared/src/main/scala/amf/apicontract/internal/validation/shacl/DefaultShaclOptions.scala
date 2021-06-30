package amf.apicontract.internal.validation.shacl

import amf.core.client.scala.config.AMFEventListener
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DataNodeModel
import amf.core.internal.validation.core.ShaclValidationOptions

case class DefaultShaclOptions(override val listeners: Seq[AMFEventListener]) extends ShaclValidationOptions {
  override val filterFields: Field => Boolean = (f: Field) => f.`type` == DataNodeModel
}
