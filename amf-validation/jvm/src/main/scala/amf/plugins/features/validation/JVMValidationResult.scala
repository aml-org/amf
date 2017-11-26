package amf.plugins.features.validation

import amf.core.validation.core.ValidationResult
import amf.core.vocabulary.Namespace
import org.json4s.JObject
import org.json4s.JsonAST.JString

/**
  * A SHACL ValidationResult
  *
  * @param value parsed JSON-LD node for the validation result reosurce
  */
case class JVMValidationResult(value: JObject) extends ValidationResult with JSONLDParser {

  val SHACL_MESSAGE = "http://www.w3.org/ns/shacl#resultMessage"
  val SHACL_PATH = "http://www.w3.org/ns/shacl#resultPath"
  val SHACL_CONSTRAINT = "http://www.w3.org/ns/shacl#sourceConstraintComponent"
  val SHACL_FOCUS_NODE = "http://www.w3.org/ns/shacl#focusNode"
  val SHACL_SEVERITY = "http://www.w3.org/ns/shacl#resultSeverity"
  val SHACL_SOURCE_SHAPE = "http://www.w3.org/ns/shacl#sourceShape"

  override def message: Option[String] = extractValue(value, SHACL_MESSAGE) match {
    case Some(JString(s)) => Some(s)
    case _ => None
  }

  override def path: String = extractId(value, SHACL_PATH).getOrElse("")

  override def sourceConstraintComponent: String = extractId(value, SHACL_CONSTRAINT).getOrElse("")

  override def focusNode: String = extractId(value, SHACL_FOCUS_NODE).getOrElse("")

  override def severity: String = extractId(value, SHACL_SEVERITY).getOrElse("#Violation").split("#").last

  /**
    * Returns the Shape holding declaring the violated constraint.
    * If it is an AMF validation, we always report the URI of th node shape, never the property shape
    * @return
    */
  override def sourceShape: String = {
    val result = extractId(value, SHACL_SOURCE_SHAPE).getOrElse("")
    if (result.startsWith(Namespace.AmfParser.base) && result.endsWith("#property")) {
      // This is an AMF property shape validation, compute the shape URI by removing the #property at the end
      result.split("#").head
    } else {
      // Normal shape, just remove the URI
      result
    }
  }
}
