package amf.domain

import amf.common.AMFAST
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

  case class SourceAST(ast: AMFAST) extends Annotation {
    override val name: String = "source-ast"

    override val value: String = null
  }

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

  case class SynthesizedField() extends Annotation {
    override val name: String = "synthesized-field"

    override val value: String = null
  }

  case class UriParameters() extends Annotation {
    override val name: String = "uri-parameters"

    override val value: String = null
  }

  case class EndPointBodyParameter() extends Annotation {

    /** Extension name. */
    override val name: String = "endpoint-body-parameter"

    /** Value as string. */
    override val value: String = null
  }

  case class OperationBodyParameter() extends Annotation {

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
