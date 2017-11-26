package amf.plugins.document.webapi.annotations

import amf.core.model.domain._
import amf.core.parser.Range
import amf.core.remote._
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.plugins.domain.webapi.models.EndPoint
import org.yaml.model.YPart




case class ParsedJSONSchema(rawText: String) extends SerializableAnnotation {
  override val name: String  = "parsed-json-schema"
  override val value: String = rawText
}

object ParsedJSONSchema extends AnnotationGraphLoader  {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ParsedJSONSchema(annotatedValue)
  }
}

case class SourceVendor(vendor: Vendor) extends SerializableAnnotation {
  override val name: String = "source-vendor"

  override val value: String = vendor.name
}

object SourceVendor extends AnnotationGraphLoader {
  def apply(vendor: String): SourceVendor = vendor match {
    case Raml.name => SourceVendor(Raml)
    case Oas.name  => SourceVendor(Oas)
    case Amf.name  => SourceVendor(Amf)
    case "RAML 1.0" => SourceVendor(Raml)
    case "OAS 2.0" => SourceVendor(Oas)
    case "AMF Graph" => SourceVendor(Amf)
    case _         => SourceVendor(Unknown)
  }

  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue match {
      case Vendor(vendor) => SourceVendor(vendor)
      case _              => throw new RuntimeException(s"Illegal vendor: '$annotatedValue'")
    }
  }
}

case class DeclaredElement() extends SerializableAnnotation {
  override val name: String = "declared-element"

  override val value: String = ""
}

object DeclaredElement extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    DeclaredElement()
  }
}



case class InlineDefinition() extends Annotation

case class SynthesizedField() extends SerializableAnnotation {
  override val name: String  = "synthesized-field"
  override val value: String = "true"
}

object SynthesizedField extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    SynthesizedField()
  }
}

case class DomainElementReference(name: String, ref: Option[DomainEntity]) extends SerializableAnnotation {
  override val value: String = name
}

object DomainElementReference extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = ???
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
object SingleValueArray extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    SingleValueArray()
  }
}

case class Inferred() extends Annotation

case class Aliases(aliases: Set[(String,String)]) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "aliases-array"

  /** Value as string. */
  override val value: String = aliases.map { case (alias, path) => s"$alias->$path" }.mkString(",")

}

object Aliases extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    Aliases(
      annotatedValue
        .split(",")
        .map(_.split("->") match {
          case Array(alias, url) => alias -> url
        })
        .toSet)
  }
}

object WebApiAnnotations {

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


  private def aliases(value: String, objects: Map[String, AmfElement]) = {
    Aliases.unparse(value, objects)
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

  private def lexical(value: String, objects: Map[String, AmfElement]) = {
    LexicalInformation(Range.apply(value))
  }
}