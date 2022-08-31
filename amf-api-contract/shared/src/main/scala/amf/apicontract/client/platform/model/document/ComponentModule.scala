package amf.apicontract.client.platform.model.document

import amf.apicontract.client.platform.model.domain.common.VersionedAmfObject
import amf.apicontract.client.scala.model.document.{ComponentModule => InternalComponentModule}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.document.Module
import amf.core.client.platform.model.domain.NamedAmfObject

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Component Module model class */
@JSExportAll
class ComponentModule(private[amf] override val _internal: InternalComponentModule)
    extends Module
    with NamedAmfObject
    with VersionedAmfObject {

  @JSExportTopLevel("ComponentModule")
  def this() = this(InternalComponentModule())

  /** Return AmfObject name. */
  override def name: StrField = _internal.name

  /** Update AmfObject name. */
  override def withName(name: String): ComponentModule.this.type = {
    _internal.withName(name)
    this
  }

  /** Return AmfObject version. */
  override def version: StrField = _internal.version

  /** Update AmfObject version. */
  override def withVersion(version: String): ComponentModule.this.type = {
    _internal.withVersion(version)
    this
  }
}
