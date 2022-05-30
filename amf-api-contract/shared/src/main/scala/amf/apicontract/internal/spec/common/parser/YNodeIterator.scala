package amf.apicontract.internal.spec.common.parser

import amf.core.internal.utils.AliasCounter
import amf.core.internal.validation.CoreValidations.ExceededMaxYamlReferences
import org.yaml.model.{YMap, YNode, YSequence, YType}

/** Lazy iterator implementation for traversing all nested nodes starting from a provided YNode. Alias counter is used
  * to avoid excessive use of anchor references which may lead to never ending processing
  */
class YNodeIterator private (var buffer: List[YNode], refsCounter: AliasCounter)(implicit ctx: WebApiContext)
    extends Iterator[YNode] {

  def this(node: YNode)(implicit ctx: WebApiContext) = {
    this(List(node), AliasCounter(ctx.options.getMaxYamlReferences))
    advance()
  }

  override def hasNext: Boolean = buffer.nonEmpty

  override def next: YNode = {
    val next = buffer.head
    if (refsCounter.exceedsThreshold(next)) {
      ctx.violation(
        ExceededMaxYamlReferences,
        "",
        "Exceeded maximum yaml references threshold"
      )
      buffer = Nil
      next
    } else {
      buffer = buffer.tail
      advance()
      next
    }
  }

  private def advance(): Unit = {
    if (buffer.nonEmpty) {
      val head = buffer.head
      head.tagType match {
        case YType.Map =>
          val m = head.as[YMap]
          val nodes = m.entries.flatMap { entry =>
            List(entry.key, entry.value)
          }
          buffer = head :: nodes.toList ++ buffer.tail
        case YType.Seq =>
          val s = head.as[YSequence]
          buffer = s :: s.nodes.toList ++ buffer.tail
        case _ =>
      }
    }
  }
}
