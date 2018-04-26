package amf.client.model

import amf.core.model.domain.{AmfObject => InternalAmfObject}

/**
  * Base class for all  the native wrappers
  */
trait AmfObjectWrapper {
  private[amf] val _internal: InternalAmfObject
}
