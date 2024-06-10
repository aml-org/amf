package amf.shapes.client.platform.model.document

import amf.core.client.platform.model.document.Document
import amf.shapes.client.scala.model.document.{AvroSchemaDocument => InternalAvroSchemaDocument}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class AvroSchemaDocument(override private[amf] val _internal: InternalAvroSchemaDocument)
    extends Document(_internal) {

  @JSExportTopLevel("AvroSchemaDocument")
  def this() = this(InternalAvroSchemaDocument())
}
