package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.templates.{AbstractDeclaration => InternalAbstractDeclaration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.AbstractDeclaration")
abstract class AbstractDeclaration(private[amf] val _internal: InternalAbstractDeclaration)
    extends DomainElement
    with Linkable {

  def name: StrField                  = _internal.name
  def description: StrField           = _internal.description
  def dataNode: DataNode              = _internal.dataNode
  def variables: ClientList[StrField] = _internal.variables.asClient

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withDataNode(dataNode: DataNode): this.type = {
    _internal.withDataNode(dataNode._internal)
    this
  }

  def withVariables(variables: ClientList[String]): this.type = {
    _internal.withVariables(variables.asInternal)
    this
  }
}
