package amf.plugins.document.webapi.parser.spec.common

import amf.core.parser.Annotations
import org.yaml.model.{IllegalTypeHandler, YMap, YMapEntry, YNode}

object YMapEntryLike {
  def apply(entry: YMapEntry)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike = RealYMapEntryLike(entry)
  def apply(node: YNode)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike      = YNodeYMapEntryLike(node)
}

sealed trait YMapEntryLike {
  def key: Option[YNode]
  def value: YNode
  def asMap: YMap
  def annotations: Annotations
}

private case class RealYMapEntryLike(e: YMapEntry)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode] = Some(e.key)
  override def value: YNode       = e.value
  override def asMap: YMap        = e.value.as[YMap]

  override def annotations: Annotations = Annotations(e)
}

private case class YNodeYMapEntryLike(n: YNode)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode] = None
  override def value: YNode       = n
  override def asMap: YMap        = n.as[YMap]

  override def annotations: Annotations = Annotations(n.value)
}
