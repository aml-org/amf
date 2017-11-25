package amf.model

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Module model class
  */
@JSExportAll
case class Module(private[amf] val model: amf.core.model.document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  @JSExportTopLevel("Module")
  def this() = this(amf.core.model.document.Module())
}
