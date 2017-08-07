package amf.model

import amf.model.builder.CreativeWorkBuilder

/**
  * CreativeWork jvm class
  */
case class CreativeWork private[model] (private[amf] val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  val url: String = creativeWork.url

  val description: String = creativeWork.description

  def toBuilder: CreativeWorkBuilder = CreativeWorkBuilder(creativeWork.toBuilder)
}
