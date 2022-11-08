package amf.shapes.client.platform.model.domain.federation
import amf.shapes.client.scala.model.domain.federation.{KeyMapping => InternalKeyMapping}
import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait KeyMapping extends DomainElement {
  type Source
  type Target
  type WithTarget

  private[amf] val _internal: InternalKeyMapping

  def source: Source
  def target: Target

  def withSource(source: Source): this.type

  def withTarget(target: WithTarget): this.type
}
