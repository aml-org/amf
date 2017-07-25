package amf.domain

import amf.builder.LicenseBuilder
import amf.metadata.domain.LicenseModel.{Name, Url}

/**
  * License internal model
  */
case class License(fields: Fields) extends DomainElement {

  override type T = License

  val url: String  = fields get Url
  val name: String = fields get Name

  def canEqual(other: Any): Boolean = other.isInstanceOf[License]

  override def equals(other: Any): Boolean = other match {
    case that: License =>
      (that canEqual this) &&
        url == that.url &&
        name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(url, name)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"License($url, $name)"

  override def toBuilder: LicenseBuilder = LicenseBuilder(fields)
}
