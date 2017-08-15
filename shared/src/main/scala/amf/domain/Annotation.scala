package amf.domain

import amf.model.AmfElement
import amf.parser.Range

/**
  * Annotation type
  */
trait Annotation {

  /** Extension name. */
  val name: String

  /** Value as string. */
  val value: String
}

object Annotation {

  case class LexicalInformation(range: Range) extends Annotation {
    override val name: String = "lexical"

    override val value: String = range.toString
  }

  case class ParentEndPoint(parent: EndPoint) extends Annotation {
    override val name: String = "parent-end-point"

    override val value: String = parent.id
  }

  case class ExplicitField() extends Annotation {
    override val name: String = "explicit-field"

    override val value: String = null
  }

  case class UriParameters() extends Annotation {
    override val name: String = "uri-parameters"

    override val value: String = null
  }

  case class EndPointBodyParameter(asParameter: Parameter) extends Annotation {

    /** Extension name. */
    override val name: String = "endpoint-body-parameter"

    /** Value as string. */
    override val value: String = null
  }

  case class OperationBodyParameter(asParameter: Parameter) extends Annotation {

    /** Extension name. */
    override val name: String = "operation-body-parameter"

    /** Value as string. */
    override val value: String = null
  }

  case class OverrideEndPointBodyParameter(asParameter: Parameter, asPayload: Payload) extends Annotation {

    /** Extension name. */
    override val name: String = "override-endpoint-body-parameter"

    /** Value as string. */
    override val value: String = null
  }

  case class MediaType(mediaType: String) extends Annotation {

    /** Extension name. */
    override val name: String = "inherith-media-type-body-parameter"

    /** Value as string. */
    override val value: String = null

    val key = "x-media-type"
  }

  case class ArrayFieldAnnotations(holder: Map[Any, List[Annotation]] = Map()) extends Annotation {
    def +(value: Any, annotations: List[Annotation]): ArrayFieldAnnotations =
      ArrayFieldAnnotations(value match {
        case l: List[_] => l.foldLeft(holder)((newHolder, v) => newHolder + (v -> annotations))
        case _          => holder + (value -> annotations)
      })

    def apply(fieldValue: Any): List[Annotation] = holder.getOrElse(fieldValue, Nil)

    override val name: String = "array-field-annotation"

    override val value: String = null
  }

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

case class EndPointPath(parent: Option[EndPointPath] = None, path: String) {

  def completePath: String = if (parent.isDefined) parent.get.completePath else "" + path
}
