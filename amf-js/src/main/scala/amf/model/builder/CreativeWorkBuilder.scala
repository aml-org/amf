package amf.model.builder

import amf.model.CreativeWork

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class CreativeWorkBuilder(
    private val creativeWorkBuilder: amf.builder.CreativeWorkBuilder = amf.builder.CreativeWorkBuilder())
    extends Builder {

  def withUrl(url: String): CreativeWorkBuilder = {
    creativeWorkBuilder.withUrl(url)
    this
  }

  def withDescription(description: String): CreativeWorkBuilder = {
    creativeWorkBuilder.withDescription(description)
    this
  }

  def build: CreativeWork = CreativeWork(creativeWorkBuilder.build)
}
