package amf.plugins.document.webapi.parser.spec.common

import amf.core.parser.Annotations
import org.yaml.model.{IllegalTypeHandler, YMap, YMapEntry, YNode, YPart, YSequence}

object YMapEntryLike {
  def apply(entry: YMapEntry)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike = RealYMapEntryLike(entry)
  def apply(node: YNode)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike      = YNodeYMapEntryLike(node)
  def apply(key: String, node: YNode)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike =
    TextKeyYMapEntryLike(key, node)
}

sealed trait YMapEntryLike {
  def key: Option[YNode]
  def keyText: Option[String]
  def ast: YPart
  def value: YNode
  def asMap: YMap
  def asSequence: YSequence
  def annotations: Annotations
}

private case class RealYMapEntryLike(e: YMapEntry)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode]       = Some(e.key)
  override def keyText: Option[String]  = e.key.asScalar.map(_.text)
  override def value: YNode             = e.value
  override def asMap: YMap              = e.value.as[YMap]
  override def asSequence: YSequence    = e.value.as[YSequence]
  override def ast: YPart               = e
  override def annotations: Annotations = Annotations(e)
}

private case class YNodeYMapEntryLike(n: YNode)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode]       = None
  override def keyText: Option[String]  = None
  override def value: YNode             = n
  override def asMap: YMap              = n.as[YMap]
  override def asSequence: YSequence    = n.as[YSequence]
  override def ast: YPart               = n
  override def annotations: Annotations = Annotations(n.value)
}

private case class TextKeyYMapEntryLike(artificialKey: String, n: YNode)(implicit errorHandler: IllegalTypeHandler)
    extends YMapEntryLike {
  override def key: Option[YNode]       = None
  override def keyText: Option[String]  = Some(artificialKey)
  override def value: YNode             = n
  override def asMap: YMap              = n.as[YMap]
  override def asSequence: YSequence    = n.as[YSequence]
  override def ast: YPart               = n
  override def annotations: Annotations = Annotations(n.value) // should be inferred?
}
