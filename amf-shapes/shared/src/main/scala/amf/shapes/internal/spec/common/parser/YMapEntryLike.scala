package amf.shapes.internal.spec.common.parser

import amf.core.internal.parser.domain.Annotations
import org.mulesoft.lexer.{Position, SourceLocation}
import org.yaml.model._

object YMapEntryLike {
  def apply(entry: YMapEntry)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike = RealYMapEntryLike(entry)
  def apply(node: YNode)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike      = YNodeYMapEntryLike(node)
  def apply(key: String, node: YNode)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike =
    TextKeyYMapEntryLike(key, node)
  def buildFakeMapEntry(entry: YMapEntry)(implicit errorHandler: IllegalTypeHandler): YMapEntryLike =
    FakeYMapEntryLike(entry)
}

sealed trait YMapEntryLike {
  def key: Option[YNode]
  def keyText: Option[String]
  def ast: YPart
  def value: YNode
  def asMap: YMap
  def asSequence: YSequence
  def annotations: Annotations

  /** Annotations for explicit fields. If YMapEntry then Annotations(ast) if other then Annotations.Inferred
    * @return
    */
  def fieldAnnotations: Annotations
}

private case class RealYMapEntryLike(e: YMapEntry)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode]       = Some(e.key)
  override def keyText: Option[String]  = e.key.asScalar.map(_.text)
  override def value: YNode             = e.value
  override def asMap: YMap              = e.value.as[YMap]
  override def asSequence: YSequence    = e.value.as[YSequence]
  override def ast: YPart               = e
  override def annotations: Annotations = Annotations(e)

  /** Annotations for explicit fields. If YMapEntry then Annotations(ast) if other then Annotations.Inferred
    *
    * @return
    */
  override def fieldAnnotations: Annotations = annotations
}

private case class YNodeYMapEntryLike(n: YNode)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode]       = None
  override def keyText: Option[String]  = None
  override def value: YNode             = n
  override def asMap: YMap              = n.as[YMap]
  override def asSequence: YSequence    = n.as[YSequence]
  override def ast: YPart               = n
  override def annotations: Annotations = Annotations(n.value)

  /** Annotations for explicit fields. If YMapEntry then Annotations(ast) if other then Annotations.Inferred
    *
    * @return
    */
  override def fieldAnnotations: Annotations = Annotations.inferred()
}

private case class TextKeyYMapEntryLike(artificialKey: String, n: YNode)(implicit errorHandler: IllegalTypeHandler)
    extends YMapEntryLike {
  override def key: Option[YNode]       = None
  override def keyText: Option[String]  = Some(artificialKey)
  override def value: YNode             = n
  override def asMap: YMap              = n.as[YMap]
  override def asSequence: YSequence    = n.as[YSequence]
  override def ast: YPart               = n
  override def annotations: Annotations = Annotations(n.value)

  /** Annotations for explicit fields. If YMapEntry then Annotations(ast) if other then Annotations.Inferred
    *
    * @return
    */
  override def fieldAnnotations: Annotations = Annotations.inferred()
}

private case class FakeYMapEntryLike(e: YMapEntry)(implicit errorHandler: IllegalTypeHandler) extends YMapEntryLike {
  override def key: Option[YNode]       = Some(e.key)
  override def keyText: Option[String]  = e.key.asScalar.map(_.text)
  override def value: YNode             = e.value
  override def asMap: YMap              = e.value.as[YMap]
  override def asSequence: YSequence    = e.value.as[YSequence]
  override def ast: YPart               = if (hasChildren) astWithCorrectLocation else e.value.value
  override def annotations: Annotations = Annotations(e)

  /** Annotations for explicit fields. If YMapEntry then Annotations(ast) if other then Annotations.Inferred
    *
    * @return
    */
  override def fieldAnnotations: Annotations = annotations

  private def astWithCorrectLocation = YMap(getCorrectLocation, e.value.value.children)

  private def getCorrectLocation = {
    val actualLocation = e.value.value.location
    val start          = extractCorrectStartPosition
    val end            = Position(actualLocation.lineTo, actualLocation.columnTo, actualLocation.offsetTo)
    SourceLocation(actualLocation.sourceName, start, end)
  }

  private def extractCorrectStartPosition = {
    val startingChildLocation = e.value.value.children.head.location
    Position(startingChildLocation.lineFrom, startingChildLocation.columnFrom, startingChildLocation.offsetFrom)
  }

  private def hasChildren = e.value.value.children.nonEmpty
}
