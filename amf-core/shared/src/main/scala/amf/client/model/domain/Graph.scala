package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.core.model.domain.{Graph => InternalGraph}
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Graph(private[amf] val _internal: InternalGraph) {
  def types(): ClientList[String] = _internal.types().asClient

  def properties(): ClientList[String] = _internal.properties().asClient

  def scalarByProperty(id: String): ClientList[Any] = _internal.scalarByProperty(id).asClient

  def getObjectByPropertyId(id: String): ClientList[DomainElement] = _internal.getObjectByPropertyId(id).asClient

  def remove(uri: String): this.type = {
    _internal.removeField(uri)
    this
  }
}