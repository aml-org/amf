package amf.model.document

import amf.core.model.document

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Module model class
  */
@JSExportAll
case class Module(private[amf] val model: document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  @JSExportTopLevel("Module")
  def this() = this(document.Module())

}
