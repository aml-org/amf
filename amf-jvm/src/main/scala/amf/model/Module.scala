package amf.model
import java.util

import amf.document

import scala.collection.JavaConverters._

/**
  * JVM Module model class
  */
case class Module(private[amf] val model: amf.document.Module) extends BaseUnit with DeclaresModel {

  /** Returns the modules references by this one.
    * A Module only can reference other module and not any other Base Unit Type */
  override val references: util.List[BaseUnit] = model.references
    .map({ case m: document.Module => Module(m) })
    .map({ bu: BaseUnit =>
      bu
    })
    .asJava

  /** Returns the file location for the document that has been parsed to generate this model */
  override val location: String = model.location

  override def unit: document.BaseUnit = model

  override def usage: String = model.usage

  override private[amf] def element = model
}
