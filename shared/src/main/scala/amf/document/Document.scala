package amf.document

import amf.domain.DomainElement
import amf.remote.URL

/**
  * A [[Document]] is a parsing Unit that encodes a stand-alone [[DomainElement]] and can include references to other
  * [[DomainElement]]s that reference from the encoded [[DomainElement]]
  */
case class Document(location: URL, references: Seq[URL], encodes: DomainElement)
    extends BaseUnit
    with EncodesModel
    with DeclaresModel {

  val declares: Seq[DomainElement] = List(encodes)
}
