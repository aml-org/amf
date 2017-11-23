package amf.plugins.domain.webapi.models.annotations

import amf.domain._
import amf.domain.dialects.DomainEntity
import amf.framework.model.domain.{Annotation, AnnotationGraphLoader, LexicalInformation, SerializableAnnotation}
import amf.model.AmfElement
import amf.parser.Range
import amf.remote._
import org.yaml.model.YPart

case class ParsedFromTypeExpression(expression: String) extends SerializableAnnotation {
  override val name: String  = "type-exprssion"
  override val value: String = expression
}

object ParsedFromTypeExpression extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ParsedFromTypeExpression(annotatedValue)
  }
}

case class ParentEndPoint(parent: EndPoint) extends SerializableAnnotation {
  override val name: String = "parent-end-point"

  override val value: String = parent.id

}

object ParentEndPoint extends AnnotationGraphLoader  {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ParentEndPoint(objects(annotatedValue).asInstanceOf[EndPoint])
  }
}

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

case class SourceAST(ast: YPart) extends Annotation

case class InlineDefinition() extends Annotation

case class ExplicitField() extends Annotation

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

case class Aliases(aliases: Set[String]) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "aliases-array"

  /** Value as string. */
  override val value: String = aliases.mkString(",")

}

object Aliases extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    Aliases(annotatedValue.split(",").toSet)
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
    Aliases(value.split(",").toSet)
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