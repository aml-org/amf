package amf.domain

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
  case class ParentEndPoint(parentPath: EndPointPath) extends Annotation {
    override val name: String  = "parent-end-point"
    override val value: String = null
  }

  case class ExplicitField() extends Annotation {
    override val name: String = "explicit-field"

    override val value: String = null
  }

  case class UriParameter() extends Annotation {
    override val name: String = "uri-parameter"

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

  def unapply(annotation: String): Option[(String) => Annotation] = annotation match {
    case "lexical" => Some((value: String) => LexicalInformation(Range.apply(value)))
    case _         => None // Unknown annotation
  }

}

case class EndPointPath(parent: Option[EndPointPath] = None, path: String) {

  def completePath: String = if (parent.isDefined) parent.get.completePath else "" + path
}
