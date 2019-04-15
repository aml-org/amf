package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.domain.shapes.models.{Examples => InternalExamples}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Examples model class
  */
@JSExportAll
case class Examples(override private[amf] val _internal: InternalExamples) extends DomainElement with Linkable {

  @JSExportTopLevel("model.domain.Examples")
  def this() = this(InternalExamples())

  def examples: ClientList[Example] = _internal.examples.asClient

  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  override def linkCopy(): Examples = _internal.linkCopy()
}
