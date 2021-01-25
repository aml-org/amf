package amf.client.`new`.amfcore

import amf.core.model.document.BaseUnit

trait AmfEventListener {
  def event(eventKind: String, bu: BaseUnit)
}
