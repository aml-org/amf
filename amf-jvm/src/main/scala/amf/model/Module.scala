package amf.model

/**
  * JVM Module model class
  */
case class Module(private[amf] val model: amf.document.Module) extends BaseUnit with DeclaresModel {

  override private[amf] val element = model

  def this() = this(amf.document.Module())
}
