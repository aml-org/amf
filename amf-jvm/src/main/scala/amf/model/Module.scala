package amf.model

/**
  * JVM Module model class
  */
case class Module(private[amf] val model: amf.core.model.document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  def this() = this(amf.core.model.document.Module())
}
