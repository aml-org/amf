package amf.core.model.domain

import amf.core.parser.Range
import amf.core.registries.AMFDomainRegistry
import org.yaml.model.YPart

trait Annotation

trait SerializableAnnotation extends Annotation {

  /** Extension name. */
  val name: String

  /** Value as string. */
  val value: String
}

trait AnnotationGraphLoader {
  def unparse(annotatedValue: String, objects: Map[String, AmfElement]): Annotation
}

case class LexicalInformation(range: Range) extends SerializableAnnotation {
  override val name: String = "lexical"

  override val value: String = range.toString
}

case class ScalarType(datatype: String) extends Annotation

object LexicalInformation extends AnnotationGraphLoader  {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    LexicalInformation(Range.apply(annotatedValue))
  }
}

case class SourceAST(ast: YPart) extends Annotation

object Annotation {
  def unapply(annotation: String): Option[(String, Map[String, AmfElement]) => Annotation] =
    AMFDomainRegistry.annotationsRegistry.get(annotation) match {
      case Some(annotationLoader) => Some(annotationLoader.unparse)
      case _                     => None
    }
}