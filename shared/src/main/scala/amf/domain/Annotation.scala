package amf.domain

import amf.common.AMFAST
import amf.model.AmfElement
import amf.parser.Range

/**
  * Annotation type
  */
trait Annotation

trait SerializableAnnotation extends Annotation {

  /** Extension name. */
  val name: String

  /** Value as string. */
  val value: String
}

object Annotation {

  case class LexicalInformation(range: Range) extends SerializableAnnotation {
    override val name: String = "lexical"

    override val value: String = range.toString
  }

  case class ParentEndPoint(parent: EndPoint) extends SerializableAnnotation {
    override val name: String = "parent-end-point"

    override val value: String = parent.id
  }

  case class SourceAST(ast: AMFAST) extends Annotation

  case class ExplicitField() extends Annotation

  case class SynthesizedField() extends Annotation

  case class UriParameters() extends Annotation

  case class EndPointBodyParameter() extends Annotation

  case class OperationBodyParameter() extends Annotation

  case class OverrideEndPointBodyParameter(asParameter: Parameter, asPayload: Payload) extends Annotation

  case class MediaType(mediaType: String) extends Annotation

  case class EndPointParameter() extends Annotation

  def unapply(annotation: String): Option[(String, Map[String, AmfElement]) => Annotation] =
    annotation match {
      case "lexical"          => Some(lexical)
      case "parent-end-point" => Some(parentEndPoint)
      case _                  => None // Unknown annotation
    }

  private def parentEndPoint(value: String, objects: Map[String, AmfElement]) = {
    ParentEndPoint(objects(value).asInstanceOf[EndPoint])
  }

  private def lexical(value: String, objects: Map[String, AmfElement]) = {
    LexicalInformation(Range.apply(value))
  }
}
