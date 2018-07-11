package amf.client.model

import amf.core.model.domain.{AmfObject => InternalAmfObject}
import amf.client.convert.CoreClientConverters._

/**
  * Base class for all  the native wrappers
  */
trait AmfObjectWrapper extends Annotable {
  private[amf] val _internal: InternalAmfObject

  override def annotations(): Annotations = _internal.annotations
}
