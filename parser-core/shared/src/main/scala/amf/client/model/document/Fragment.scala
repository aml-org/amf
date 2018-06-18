package amf.client.model.document

import amf.core.model.document.{Fragment => InternalFragment}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Fragment(private[amf] val _internal: InternalFragment) extends BaseUnit with EncodesModel
