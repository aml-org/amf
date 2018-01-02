package amf.model.document

import amf.core.model.document
import amf.core.remote.AmfObjectWrapper
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.model.domain.DomainElement

import scala.collection.JavaConverters._

/** Any parsable unit, backed by a source URI. */
trait BaseUnit extends AmfObjectWrapper with PlatformSecrets {

  private[amf] val element: document.BaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references(): java.util.List[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map { e =>
      { platform.wrap[BaseUnit](e) }
    }
    units.asJava
  }

  def withReferences(newReferences: java.util.List[BaseUnit]) = {
    val refs = newReferences.asScala.map(_.element)
    element.withReferences(refs)
  }

  def raw: String = element.raw.orNull

  def withRaw(raw: String) = {
    element.withRaw(raw)
    this
  }

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = element.location
  def withLocation(location: String) = {
    element.withLocation(location)
    this
  }

  def usage: String = element.usage

  def findById(id: String): DomainElement = {
    element.findById(Namespace.uri(id).iri()) match {
      case Some(e: DomainElement) => platform.wrap[DomainElement](e)
      case _                      => null
    }
  }

  def findByType(typeId: String): java.util.List[DomainElement] =
    element.findByType(Namespace.expand(typeId).iri()).map(e => platform.wrap[DomainElement](e)).asJava
}
