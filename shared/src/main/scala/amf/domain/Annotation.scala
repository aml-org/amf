package amf.domain

import amf.model.AmfElement
import amf.parser.Range
import amf.remote.Vendor
import org.yaml.model.YPart

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

  case class ParsedFromTypeExpression(expression: String) extends SerializableAnnotation {
    override val name: String  = "type-exprssion"
    override val value: String = expression
  }

  case class LexicalInformation(range: Range) extends SerializableAnnotation {
    override val name: String = "lexical"

    override val value: String = range.toString
  }

  case class ParentEndPoint(parent: EndPoint) extends SerializableAnnotation {
    override val name: String = "parent-end-point"

    override val value: String = parent.id
  }

  case class ParsedJSONSchema(rawText: String) extends SerializableAnnotation {
    override val name: String  = "parsed-json-schema"
    override val value: String = rawText
  }

  case class SourceVendor(vendor: Vendor) extends SerializableAnnotation {
    override val name: String = "source-vendor"

    override val value: String = vendor.name
  }

  case class DeclaredElement() extends SerializableAnnotation {
    override val name: String = "declared-element"

    override val value: String = ""
  }

  case class ScalarType(datatype: String) extends Annotation

  case class SourceAST(ast: YPart) extends Annotation

  case class InlineDefinition() extends Annotation

  case class ExplicitField() extends Annotation

  case class SynthesizedField() extends SerializableAnnotation {
    override val name: String  = "synthesized-field"
    override val value: String = "true"
  }

  case class EndPointBodyParameter() extends Annotation

  case class DefaultPayload() extends Annotation

  case class EndPointParameter() extends Annotation

  // save original text link?
  case class ReferencedElement(parsedUrl: String, referenced: DomainElement) extends Annotation

  case class SingleValueArray() extends SerializableAnnotation {

    /** Extension name. */
    override val name: String = "single-value-array"

    /** Value as string. */
    override val value: String = ""
  }

  case class Inferred() extends Annotation

  case class Aliases(aliases: Set[(String, String)]) extends SerializableAnnotation {

    /** Extension name. */
    override val name: String = "aliases-array"

    /** Value as string. */
    override val value: String = aliases.map { case (alias, path) => s"$alias->$path" }.mkString(",")
  }

  def unapply(annotation: String): Option[(String, Map[String, AmfElement]) => Annotation] =
    annotation match {
      case "lexical"            => Some(lexical)
      case "parent-end-point"   => Some(parentEndPoint)
      case "source-vendor"      => Some(sourceVendor)
      case "single-value-array" => Some(singleValueArray)
      case "aliases-array"      => Some(aliases)
      case "parsed-json-schema" => Some(parsedJsonSchema)
      case "declared-element"   => Some(declaredElement)
      case "type-exprssion"     => Some(typeExpression)
      case "synthesized-field"  => Some(synthesizedField)
      case _                    => None // Unknown annotation
    }

  private def sourceVendor(value: String, objects: Map[String, AmfElement]) = {
    value match {
      case Vendor(vendor) => SourceVendor(vendor)
      case _              => throw new RuntimeException(s"Illegal vendor: '$value'")
    }
  }

  private def parentEndPoint(value: String, objects: Map[String, AmfElement]) = {
    ParentEndPoint(objects(value).asInstanceOf[EndPoint])
  }

  private def singleValueArray(value: String, objects: Map[String, AmfElement]) = {
    SingleValueArray()
  }

  private def lexical(value: String, objects: Map[String, AmfElement]) = {
    LexicalInformation(Range.apply(value))
  }

  private def aliases(value: String, objects: Map[String, AmfElement]) = {
    Aliases(
      value
        .split(",")
        .map(_.split("->") match {
          case Array(alias, url) => alias -> url
        })
        .toSet)
  }

  private def parsedJsonSchema(value: String, objects: Map[String, AmfElement]) = {
    ParsedJSONSchema(value)
  }

  private def declaredElement(value: String, objects: Map[String, AmfElement]) = {
    DeclaredElement()
  }

  private def typeExpression(value: String, objects: Map[String, AmfElement]) = {
    ParsedFromTypeExpression(value)
  }

  private def synthesizedField(value: String, objects: Map[String, AmfElement]) = {
    SynthesizedField()
  }
}
