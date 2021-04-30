package amf.plugins.document.webapi.annotations

import amf.core.model.domain._
import amf.core.parser.Range
import org.yaml.model.YMapEntry

case class FormBodyParameter() extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "form-body-parameter"
  override val value: String = "true"
}

object FormBodyParameter extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = {
    Some(FormBodyParameter())
  }
}

case class BodyParameter() extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "body-parameter"
  override val value: String = "true"
}

object BodyParameter extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = {
    Some(BodyParameter())
  }
}

case class ParameterNameForPayload(paramName: String, range: Range)
    extends SerializableAnnotation
    with PerpetualAnnotation { // perpetual? after resolution i should have a normal payload
  override val name: String  = "parameter-name-for-payload"
  override val value: String = paramName + "->" + range.toString
}

object ParameterNameForPayload extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = {
    value.split("->") match {
      case Array(req, range) =>
        Some(new ParameterNameForPayload(req, Range.apply(range)))
      case _ => None
    }

  }
}

case class RequiredParamPayload(required: Boolean, range: Range)
    extends SerializableAnnotation
    with PerpetualAnnotation { // perpetual? after resolution i should have a normal payload
  override val name: String  = "required-param-payload"
  override val value: String = required + "->" + range.toString
}

object RequiredParamPayload extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = {
    value.split("->") match {
      case Array(req, range) =>
        val required = if (req.equals("true")) true else false
        Some(new RequiredParamPayload(required, Range.apply(range)))
      case _ => None
    }
  }
}

case class LocalLinkPath(rawPath: String) extends SerializableAnnotation {
  override val name: String  = "local-link-path"
  override val value: String = rawPath
}

object LocalLinkPath extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(LocalLinkPath(value))
}

case class EndPointBodyParameter() extends Annotation

case class DefaultPayload() extends Annotation

case class EmptyPayload() extends Annotation

case class EndPointParameter() extends Annotation

case class EndPointTraitEntry(range: Range) extends Annotation

case class EndPointResourceTypeEntry(range: Range) extends Annotation

case class OperationTraitEntry(range: Range) extends Annotation

// save original text link?
case class ReferencedElement(parsedUrl: String, referenced: DomainElement) extends Annotation

case class CollectionFormatFromItems() extends Annotation

case class ExternalJsonSchemaShape(original: YMapEntry) extends Annotation

// used internally for emission of links that have been inlined. This annotation is removed in resolution
case class ExternalReferenceUrl(url: String) extends Annotation

case class ForceEntry() extends Annotation

case class ExampleIndex(index: Int) extends Annotation
