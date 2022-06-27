package amf.apicontract.client.platform.model.document

import amf.apicontract.client.scala.model.document.{JsonSchemaDocument => InternalJsonSchemaDocument}
import amf.core.client.platform.model.document.Document
import amf.core.client.scala.model.StrField

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class JsonSchemaDocument(override private[amf] val _internal: InternalJsonSchemaDocument)
    extends Document(_internal) {

  @JSExportTopLevel("JsonSchemaDocument")
  def this() = this(InternalJsonSchemaDocument())

  def schemaVersion: StrField = _internal.schemaVersion
}
