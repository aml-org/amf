package amf.model

import amf.document

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Module model class
  */
@JSExportAll
case class Module(private[amf] val model: amf.document.Module) extends BaseUnit with DeclaresModel {

  /** Returns the modules references by this one.
    * A Module only can reference other module and not any other Base Unit Type */
  override val references: js.Iterable[BaseUnit] = model.references
    .map({ case m: document.Module => Module(m) })
    .map({ bu: BaseUnit =>
      bu
    })
    .toJSArray

  /** Returns the file location for the document that has been parsed to generate this model */
  override val location: String = model.location

  override def unit: document.BaseUnit = model

  override def usage: String = model.usage

  override private[amf] def element = model
}
