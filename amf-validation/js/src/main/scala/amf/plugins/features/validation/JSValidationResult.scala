package amf.plugins.features.validation

import amf.core.validation.core.ValidationResult
import amf.core.vocabulary.Namespace

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JSValidationResult(wrapped: js.Dynamic) extends  ValidationResult {
  override def message: Option[String] = JSUtils.maybe(wrapped.message())

  override def path: String = {
    val res = JSUtils.default(wrapped.path(), "")
    if (res.startsWith("_:")) { // removing blank nodes from the report
      ""
    } else {
      res
    }
  }

  override def sourceConstraintComponent: String = JSUtils.default(wrapped.sourceConstraintComponent(), "")

  override def focusNode: String = JSUtils.default(wrapped.focusNode(), "")

  override def severity: String = JSUtils.default(wrapped.severity(), "Violation")

  /**
    * Returns the Shape holding declaring the violated constraint.
    * If it is an AMF validation, we always report the URI of th node shape, never the property shape
    * @return
    */
  override def sourceShape: String = {
    val result = JSUtils.default(wrapped.sourceShape(), "")
    if (result.startsWith(Namespace.AmfParser.base) && result.endsWith("#property")) {
      // This is an AMF property shape validation, compute the shape URI by removing the #property at the end
      result.split("#").head
    } else {
      // Normal shape, just remove the URI
      result
    }
  }

}

object JSValidationResult {
  def fromDynamic(x: js.Dynamic) = new JSValidationResult(x)
}