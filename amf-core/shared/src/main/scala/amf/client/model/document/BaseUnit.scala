package amf.client.model.document

import amf.client.convert.CoreClientConverters._
import amf.client.model.{AmfObjectWrapper, StrField}
import amf.client.model.domain.DomainElement
import amf.core.model.document.{BaseUnit => InternalBaseUnit}
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace

import scala.scalajs.js.annotation.JSExportAll

/** Any parsable unit, backed by a source URI. */
@JSExportAll
trait BaseUnit extends AmfObjectWrapper with PlatformSecrets {

  override private[amf] val _internal: InternalBaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references(): ClientList[BaseUnit] = _internal.references.asClient

  /** Raw text  used to generated this unit */
  def raw: String = _internal.raw.orNull

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = _internal.location

  /** Returns the usage comment for de element */
  def usage: StrField = _internal.usage

  def withReferences(references: ClientList[BaseUnit]): this.type = {
    _internal.withReferences(references.asInternal)
    this
  }

  def withRaw(raw: String): this.type = {
    _internal.withRaw(raw)
    this
  }

  def withLocation(location: String): this.type = {
    _internal.withLocation(location)
    this
  }

  def withUsage(usage: String): this.type = {
    _internal.withUsage(usage)
    this
  }

  def findById(id: String): ClientOption[DomainElement] = _internal.findById(Namespace.uri(id).iri()).asClient

  def findByType(typeId: String): ClientList[DomainElement] =
    _internal.findByType(Namespace.expand(typeId).iri()).asClient
}
