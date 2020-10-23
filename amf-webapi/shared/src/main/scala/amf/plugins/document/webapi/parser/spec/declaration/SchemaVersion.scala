package amf.plugins.document.webapi.parser.spec.declaration

import amf.client.render.{JSONSchemaVersions => ClientJSONSchemaVersions, JSONSchemaVersion => ClientJSONSchemaVersion}
import amf.core.errorhandling.ErrorHandler
import amf.plugins.features.validation.CoreValidations

abstract class SchemaVersion(val name: String)

object SchemaVersion {
  def fromClientOptions(schema: ClientJSONSchemaVersion): SchemaVersion = schema match {
    case ClientJSONSchemaVersions.DRAFT_07 => JSONSchemaDraft7SchemaVersion
    case _                                 => JSONSchemaDraft4SchemaVersion
  }
}

abstract class RAMLSchemaVersion(override val name: String) extends SchemaVersion(name)
case class RAML10SchemaVersion()     extends RAMLSchemaVersion("raml1.0")

class OASSchemaVersion(override val name: String, val position: String)(implicit eh: ErrorHandler)
  extends SchemaVersion(name) {
  if (position != "schema" && position != "parameter")
    eh.violation(CoreValidations.ResolutionValidation,
      "",
      s"Invalid schema position '$position', only 'schema' and 'parameter' are valid")
}

class OAS20SchemaVersion(override val position: String)(implicit eh: ErrorHandler) extends OASSchemaVersion("oas2.0", position)
object OAS20SchemaVersion {
  def apply(position: String)(implicit eh: ErrorHandler) = new OAS20SchemaVersion(position)
}

class OAS30SchemaVersion(override val position: String)(implicit eh: ErrorHandler) extends OASSchemaVersion("oas3.0.0", position)
object OAS30SchemaVersion {
  def apply(position: String)(implicit eh: ErrorHandler) = new OAS30SchemaVersion(position)(eh)
}

abstract class JSONSchemaVersion(override val name: String) extends SchemaVersion(name) with Comparable[JSONSchemaVersion]{
  override def compareTo(o: JSONSchemaVersion): Int = {
    if (name.length < o.name.length) -1
    else name.compareTo(o.name)
  }
}
object JSONSchemaDraft3SchemaVersion extends JSONSchemaVersion("draft-3")
object JSONSchemaDraft4SchemaVersion extends JSONSchemaVersion("draft-4")
object JSONSchemaDraft6SchemaVersion extends JSONSchemaVersion("draft-6")
object JSONSchemaDraft7SchemaVersion extends JSONSchemaVersion("draft-7")
object JSONSchemaUnspecifiedVersion  extends JSONSchemaVersion("")