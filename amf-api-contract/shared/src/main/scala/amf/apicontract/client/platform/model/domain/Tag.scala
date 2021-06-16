package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.apicontract.client.scala.model.domain.{Tag => InternalTag}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.shapes.client.platform.model.domain.CreativeWork

/**
  * Tag model class.
  */
@JSExportAll
case class Tag(override private[amf] val _internal: InternalTag) extends DomainElement with NamedDomainElement {

  @JSExportTopLevel("model.domain.Tag")
  def this() = this(InternalTag())

  def name: StrField              = _internal.name
  def description: StrField       = _internal.description
  def documentation: CreativeWork = _internal.documentation

  /** Set name property of Tag. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of Tag. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set host property of this Server. */
  def withVariables(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }
}
