package amf.builder

import amf.metadata.domain.CreativeWorkModel.{Description, Url}
import amf.domain.{CreativeWork, Fields}

/**
  * CreativeWork domain element builder.
  */
class CreativeWorkBuilder extends Builder {

  override type T = CreativeWork

  def withUrl(url: String): CreativeWorkBuilder = set(Url, url)

  def withDescription(description: String): CreativeWorkBuilder = set(Description, description)

  override def build: CreativeWork = CreativeWork(fields)
}

object CreativeWorkBuilder {
  def apply(): CreativeWorkBuilder = new CreativeWorkBuilder()

  def apply(fields: Fields): CreativeWorkBuilder = apply().copy(fields)
}
