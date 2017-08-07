package amf.model

import amf.model.builder.CreativeWorkBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * CreativeWork js class
  */
@JSExportAll
case class CreativeWork private[model] (private[amf] val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  val url: String = creativeWork.url

  val description: String = creativeWork.description

  def toBuilder: CreativeWorkBuilder = CreativeWorkBuilder(creativeWork.toBuilder)
}
