package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField
import amf.plugins.domain.apicontract.models.{Tag => InternalTag}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

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
