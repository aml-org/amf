package amf.model.document

import amf.core.model.domain.DomainElement
import amf.core.remote.AmfObjectWrapper
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Any parsable unit, backed by a source URI. */
@JSExportAll
@JSExportTopLevel("model.document.BaseUnit")
class BaseUnit extends AmfObjectWrapper with PlatformSecrets {

  private[amf] val element: amf.core.model.document.BaseUnit = null

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references(): js.Iterable[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map (e => { platform.wrap[BaseUnit](e) })
    units.toJSArray
  }

  def withReferences(newReferences: js.Iterable[BaseUnit]) = {
    val refs = newReferences.map(_.element).toSeq
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

  def findById(id: String): amf.model.domain.DomainElement = {
    element.findById(Namespace.uri(id).iri()) match {
      case Some(e: DomainElement) => platform.wrap[amf.model.domain.DomainElement](e)
      case _                      => null
    }
  }

  def findByType(typeId: String): js.Iterable[amf.model.domain.DomainElement] =
    element
      .findByType(Namespace.expand(typeId).iri())
      .map(e => platform.wrap[amf.model.domain.DomainElement](e))
      .toJSIterable

}
