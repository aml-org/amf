package amf.builder

import amf.metadata.domain.CreativeWorkModel.{Description, Url}
import amf.domain.{Annotation, CreativeWork, Fields}

/**
  * CreativeWork domain element builder.
  */
class CreativeWorkBuilder extends Builder {

  override type T = CreativeWork

  def withUrl(url: String): CreativeWorkBuilder = set(Url, url)

  def withDescription(description: String): CreativeWorkBuilder = set(Description, description)

  override def resolveId(container: String): this.type = withId(container + "/creative-work")

  override def build: CreativeWork = null
}

object CreativeWorkBuilder {
  def apply(): CreativeWorkBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): CreativeWorkBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): CreativeWorkBuilder =
    new CreativeWorkBuilder().withAnnotations(annotations)
}
