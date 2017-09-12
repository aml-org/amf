package amf.model

/**
  * JVM XMLSerializer model class.
  */
case class XMLSerializer private[model] (private[amf] val xmlSerializer: amf.shape.XMLSerializer)
    extends DomainElement {

  def this() = this(amf.shape.XMLSerializer())

  val attribute: Boolean = xmlSerializer.attribute
  val wrapped: Boolean   = xmlSerializer.wrapped
  val name: String       = xmlSerializer.name
  val namespace: String  = xmlSerializer.namespace
  val prefix: String     = xmlSerializer.prefix

  override def equals(other: Any): Boolean = other match {
    case that: XMLSerializer =>
      (that canEqual this) &&
        xmlSerializer == that.xmlSerializer
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[XMLSerializer]

  override private[amf] def element: amf.shape.XMLSerializer = xmlSerializer

  def withAttribute(attribute: Boolean): this.type = {
    xmlSerializer.withAttribute(attribute)
    this
  }

  def withWrapped(wrapped: Boolean): this.type = {
    xmlSerializer.withWrapped(wrapped)
    this
  }

  def withName(name: String): this.type = {
    xmlSerializer.withName(name)
    this
  }

  def withNamespace(namespace: String): this.type = {
    xmlSerializer.withNamespace(namespace)
    this
  }

  def withPrefix(prefix: String): this.type = {
    xmlSerializer.withPrefix(prefix)
    this
  }
}
