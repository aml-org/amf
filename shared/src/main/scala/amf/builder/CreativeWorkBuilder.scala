package amf.builder

import amf.metadata.domain.CreativeWorkModel.{Description, Url}
import amf.model.CreativeWork

/**
  * CreativeWork domain element builder.
  */
class CreativeWorkBuilder extends Builder[CreativeWork] {

  def withUrl(url: String): CreativeWorkBuilder = set(Url, url)

  def withDescription(description: String): CreativeWorkBuilder = set(Description, description)

  override def build: CreativeWork = CreativeWork(fields)
}

object CreativeWorkBuilder {
  def apply(): CreativeWorkBuilder = new CreativeWorkBuilder()
}
