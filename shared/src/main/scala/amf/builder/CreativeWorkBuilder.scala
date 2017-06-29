package amf.builder

import amf.model.CreativeWork

/**
  * CreativeWork domain element builder.
  */
class CreativeWorkBuilder extends Builder[CreativeWork] {
  var url: String         = _
  var description: String = _

  def withUrl(url: String): CreativeWorkBuilder = {
    this.url = url
    this
  }

  def withDescription(description: String): CreativeWorkBuilder = {
    this.description = description
    this
  }

  override def build: CreativeWork = new CreativeWork(url, description)
}

object CreativeWorkBuilder {
  def apply(): CreativeWorkBuilder = new CreativeWorkBuilder()
}
