package amf.model

import amf.builder.CreativeWorkBuilder

/**
  * Domain element of type schema-org:CreativeWork
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:description
  */
class CreativeWork(val url: String, val description: String) extends DomainElement[CreativeWork, CreativeWorkBuilder] {
  override def toBuilder: CreativeWorkBuilder = ???
}
