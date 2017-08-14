package amf.model.builder

import amf.model.CreativeWork

case class CreativeWorkBuilder private (
    private val creativeWorkBuilder: amf.builder.CreativeWorkBuilder = amf.builder.CreativeWorkBuilder())
    extends Builder {

  def this() = this(amf.builder.CreativeWorkBuilder())

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
