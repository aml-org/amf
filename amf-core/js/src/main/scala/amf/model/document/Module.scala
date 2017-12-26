package amf.model.document

import amf.core.model.document

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Module model class
  */
@JSExportTopLevel("model.document.Module")
@JSExportAll
case class Module(private[amf] val model: document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  @JSExportTopLevel("model.document.Module")
  def this() = this(document.Module())

}
