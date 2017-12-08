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
  lazy val references: js.Iterable[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map { e => platform.wrap(e) }
    units.toJSArray
  }


  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = element.location

  def usage: String = element.usage

  def findById(id: String): DomainElement = {
    element.findById(Namespace.uri(id).iri()) match {
      case Some(e: DomainElement) => platform.wrap(e)
      case _                      => null
    }
  }

  def findByType(typeId: String): js.Iterable[DomainElement] =
    element.findByType(Namespace.expand(typeId).iri()).map(e => platform.wrap(e)).toJSIterable

}
