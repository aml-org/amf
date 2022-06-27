package amf.shapes.internal.spec.common

import amf.core.client.common.render.{
  JSONSchemaVersion => ClientJSONSchemaVersion,
  JSONSchemaVersions => ClientJSONSchemaVersions
}
import amf.shapes.internal.spec.common.SchemaPosition.Position

abstract class SchemaVersion(val name: String) {

  // This compare methods are shit. I had to add them to make adding JSONSchemaVersions easier. IF we could build TypeParsers dynamically we wouldn't need this.
  def isBiggerThanOrEqualTo(jsonVersion: JSONSchemaVersion): Boolean = {
    this match {
      case thisJsonVersion: JSONSchemaVersion => thisJsonVersion >= jsonVersion
      case _                                  => false
    }
  }

  def isSmallerThanOrDifferentThan(jsonVersion: JSONSchemaVersion): Boolean = {
    this match {
      case thisJsonVersion: JSONSchemaVersion => thisJsonVersion < jsonVersion
      case _                                  => true
    }
  }
}

object SchemaVersion {
  def fromClientOptions(schema: ClientJSONSchemaVersion): JSONSchemaVersion = schema match {
    case ClientJSONSchemaVersions.Draft201909 => JSONSchemaDraft201909SchemaVersion
    case ClientJSONSchemaVersions.Draft07     => JSONSchemaDraft7SchemaVersion
    case _                                    => JSONSchemaDraft4SchemaVersion
  }

  def toClientOptions(schema: SchemaVersion): ClientJSONSchemaVersion = schema match {
    case JSONSchemaDraft201909SchemaVersion => ClientJSONSchemaVersions.Draft201909
    case JSONSchemaDraft7SchemaVersion      => ClientJSONSchemaVersions.Draft07
    case _                                  => ClientJSONSchemaVersions.Draft04
  }

  def unapply(url: String): JSONSchemaVersion = url match {
    case JSONSchemaDraft3SchemaVersion.url      => JSONSchemaDraft3SchemaVersion
    case JSONSchemaDraft4SchemaVersion.url      => JSONSchemaDraft4SchemaVersion
    case JSONSchemaDraft6SchemaVersion.url      => JSONSchemaDraft6SchemaVersion
    case JSONSchemaDraft7SchemaVersion.url      => JSONSchemaDraft7SchemaVersion
    case JSONSchemaDraft201909SchemaVersion.url => JSONSchemaDraft201909SchemaVersion
    case _                                      => JSONSchemaUnspecifiedVersion
  }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~ RAML ~~~~~~~~~~~~~~~~~~~~~~~~

abstract class RAMLSchemaVersion(override val name: String) extends SchemaVersion(name)
case object RAML10SchemaVersion                             extends RAMLSchemaVersion("raml1.0")
case object RAML08SchemaVersion                             extends RAMLSchemaVersion("raml0.8")

// ~~~~~~~~~~~~~~~~~~~~~~~~ OAS ~~~~~~~~~~~~~~~~~~~~~~~~

object SchemaPosition extends Enumeration {
  type Position = Value
  val Schema: SchemaPosition.Value    = Value("schema")
  val Parameter: SchemaPosition.Value = Value("parameter")
  val Other: SchemaPosition.Value     = Value("")
}

class OASSchemaVersion(override val name: String, val position: Position) extends SchemaVersion(name)
case class OAS20SchemaVersion(override val position: Position)            extends OASSchemaVersion("oas2.0", position)
case class OAS30SchemaVersion(override val position: Position)            extends OASSchemaVersion("oas3.0.0", position)

// ~~~~~~~~~~~~~~~~~~~~~~~~ JSON Schema ~~~~~~~~~~~~~~~~~~~~~~~~

abstract class JSONSchemaVersion(override val name: String, val url: String)
    extends SchemaVersion(name)
    with Ordered[JSONSchemaVersion] {
  override def compare(that: JSONSchemaVersion): Int = {
    if (name.length < that.name.length) -1
    else if (name.length > that.name.length) 1
    else name.compareTo(that.name)
  }
}
object JSONSchemaDraft3SchemaVersion extends JSONSchemaVersion("draft-3", "http://json-schema.org/draft-03/schema#")
object JSONSchemaDraft4SchemaVersion extends JSONSchemaVersion("draft-4", "http://json-schema.org/draft-04/schema#")
object JSONSchemaDraft6SchemaVersion extends JSONSchemaVersion("draft-6", "http://json-schema.org/draft-06/schema#")
object JSONSchemaDraft7SchemaVersion extends JSONSchemaVersion("draft-7", "http://json-schema.org/draft-07/schema#")
object JSONSchemaDraft201909SchemaVersion
    extends JSONSchemaVersion("draft-2019-09", "http://json-schema.org/draft/2019-09/schema#")
object JSONSchemaUnspecifiedVersion extends JSONSchemaVersion("", "")
