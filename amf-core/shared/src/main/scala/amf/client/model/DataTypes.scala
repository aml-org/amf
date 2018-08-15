package amf.client.model

import amf.core.vocabulary.Namespace

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("DataTypes")
object DataTypes {
  val String: String       = (Namespace.Xsd + "string").iri()
  val Integer: String      = (Namespace.Xsd + "integer").iri()
  val Number: String       = (Namespace.Shapes + "number").iri()
  val Long: String         = (Namespace.Xsd + "long").iri()
  val Double: String       = (Namespace.Xsd + "double").iri()
  val Float: String        = (Namespace.Xsd + "float").iri()
  val Decimal: String      = (Namespace.Xsd + "decimal").iri()
  val Boolean: String      = (Namespace.Xsd + "boolean").iri()
  val Date: String         = (Namespace.Xsd + "date").iri()
  val Time: String         = (Namespace.Xsd + "time").iri()
  val DateTime: String     = (Namespace.Xsd + "dateTime").iri()
  val DateTimeOnly: String = (Namespace.Shapes + "dateTimeOnly").iri()
  val File: String         = (Namespace.Shapes + "file").iri()
  val Byte: String         = (Namespace.Xsd + "byte").iri()
  val Binary: String       = (Namespace.Xsd + "base64Binary").iri()
  val Password: String     = (Namespace.Shapes + "password").iri()
  val Any: String          = (Namespace.Xsd + "anyType").iri()
  val Nil: String          = (Namespace.Xsd + "nil").iri()
}
