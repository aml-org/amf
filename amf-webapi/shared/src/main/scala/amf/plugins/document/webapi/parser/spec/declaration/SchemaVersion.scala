package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.errorhandling.ErrorHandler
import amf.plugins.features.validation.CoreValidations

abstract class JSONSchemaVersion(val name: String)
class OASSchemaVersion(override val name: String, val position: String)(implicit eh: ErrorHandler)
    extends JSONSchemaVersion(name) {
  if (position != "schema" && position != "parameter")
    eh.violation(CoreValidations.ResolutionValidation,
                 "",
                 s"Invalid schema position '$position', only 'schema' and 'parameter' are valid")
}
abstract class RAMLSchemaVersion(override val name: String) extends JSONSchemaVersion(name)
class OAS20SchemaVersion(override val position: String)(implicit eh: ErrorHandler)
    extends OASSchemaVersion("oas2.0", position)
object OAS20SchemaVersion { def apply(position: String)(implicit eh: ErrorHandler) = new OAS20SchemaVersion(position) }
class OAS30SchemaVersion(override val position: String)(implicit eh: ErrorHandler)
    extends OASSchemaVersion("oas3.0.0", position)
object OAS30SchemaVersion {
  def apply(position: String)(implicit eh: ErrorHandler) = new OAS30SchemaVersion(position)(eh)
}
object JSONSchemaDraft3SchemaVersion extends JSONSchemaVersion("draft-3")
object JSONSchemaDraft4SchemaVersion extends JSONSchemaVersion("draft-4")
object JSONSchemaDraft7SchemaVersion extends JSONSchemaVersion("draft-7")
object JSONSchemaUnspecifiedVersion  extends JSONSchemaVersion("")
case class RAML10SchemaVersion()     extends RAMLSchemaVersion("raml1.0")
