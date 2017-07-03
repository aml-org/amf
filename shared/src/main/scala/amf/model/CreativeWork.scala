package amf.model

import amf.builder.CreativeWorkBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * Domain element of type schema-org:CreativeWork
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:description
  */
@JSExportAll
class CreativeWork(val url: String, val description: String) extends DomainElement[CreativeWork, CreativeWorkBuilder] {
  override def toBuilder: CreativeWorkBuilder = CreativeWorkBuilder().withUrl(url).withDescription(description)
}
