package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.Shape
import amf.core.client.platform.model.{IntField, StrField}
import amf.core.internal.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.{AnyShape => InternalAnyShape}
import amf.shapes.internal.convert.ShapeClientConverters._

class AnyShape(override private[amf] val _internal: InternalAnyShape) extends Shape with PlatformSecrets {

  @JSExportTopLevel("AnyShape")
  def this() = this(InternalAnyShape())

  @JSExport
  def documentation: CreativeWork = _internal.documentation
  @JSExport
  def xmlSerialization: XMLSerializer = _internal.xmlSerialization
  @JSExport
  def examples: ClientList[Example] = _internal.examples.asClient
  @JSExport
  def comment: StrField = _internal.comment
  @JSExport
  def namespace: StrField = _internal.namespace
  @JSExport
  def aliases: ClientList[StrField] = _internal.aliases.asClient
  @JSExport
  def size: IntField = _internal.size
  @JSExport
  def schemaVersion: StrField = _internal.schemaVersion

  @JSExport
  def withDocumentation(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }

  @JSExport
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    _internal.withXMLSerialization(xmlSerialization)
    this
  }

  @JSExport
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  @JSExport
  def withComment(comment: String): this.type = {
    _internal.withComment(comment)
    this
  }

  @JSExport
  def withExample(mediaType: String): Example = _internal.withExample(Some(mediaType))

  @JSExport
  override def linkCopy(): AnyShape = _internal.linkCopy()

  /** Aux method to know when the shape is instance only of any shape and it's because was parsed from an empty (or only
    * with example) payload, an not an explicit type def
    */
  @JSExport
  def isNotExplicit: Boolean = _internal.isNotExplicit

  /** @param trackId
    *   parent id of the original payload type with simpleinheritance and the searched example.
    * @return
    *   A ClientOption of the original inlined example, or empty if there is not any example with that track information
    *   annotated.
    */
  @JSExport
  def trackedExample(trackId: String): ClientOption[Example] = _internal.trackedExample(trackId).asClient

  // Aux method to know if the shape has the annotation of [[InlineDefinition]]
  @JSExport
  def inlined(): Boolean = _internal.inlined

  @JSExport
  def withNamespace(namespace: String): this.type = {
    _internal.withNamespace(namespace)
    this
  }

  @JSExport
  def withAliases(aliases: Seq[String]): this.type = {
    _internal.withAliases(aliases)
    this
  }

  @JSExport
  def withSize(size: Int): this.type = {
    _internal.withSize(size)
    this
  }

  @JSExport
  def withSchemaVersion(version: String): this.type = {
    _internal.withSchemaVersion(version)
    this
  }

  @JSExport
  def avroSchemaType(): ClientOption[String] = _internal.avroSchemaType.asClient

  @JSExport
  def isAvroSchema(): Boolean = _internal.isAvroSchema
}
