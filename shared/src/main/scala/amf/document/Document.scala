package amf.document

import java.net.URL

import amf.domain.DomainElement

/**
  * Document
  */
case class Document(location: URL, references: List[URL], encodes: DomainElement)
    extends Unit
    with EncodesModel
    with DeclaresModel {

  val declares: List[DomainElement] = List(encodes)
}
