package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}
import amf.core.parser.Range

case class LexicalInformation(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "lexical"

  override val value: String = range.toString
}

object LexicalInformation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): LexicalInformation =
    LexicalInformation.apply(annotatedValue)

  def apply(range: String): LexicalInformation = new LexicalInformation(Range.apply(range))
}

class HostLexicalInformation(override val range: Range) extends LexicalInformation(range) {
  override val name = "host-lexical"
}

object HostLexicalInformation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): HostLexicalInformation =
    HostLexicalInformation.apply(Range(annotatedValue))

  def apply(range: Range): HostLexicalInformation = new HostLexicalInformation(range)
}

class BasePathLexicalInformation(override val range: Range) extends LexicalInformation(range) {
  override val name = "base-path-lexical"
}

object BasePathLexicalInformation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): BasePathLexicalInformation =
    BasePathLexicalInformation.apply(Range(annotatedValue))

  def apply(range: Range): BasePathLexicalInformation = new BasePathLexicalInformation(range)
}
