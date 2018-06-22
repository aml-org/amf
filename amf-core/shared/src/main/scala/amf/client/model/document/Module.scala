package amf.client.model.document

import amf.core.model.document.{Module => InternalModule}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Module model class
  */
@JSExportAll
@JSExportTopLevel("model.document.Module")
case class Module(private[amf] val _internal: InternalModule) extends BaseUnit with DeclaresModel {

  @JSExportTopLevel("model.document.Module")
  def this() = this(InternalModule())
}
