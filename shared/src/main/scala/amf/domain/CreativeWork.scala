package amf.domain

import amf.builder.CreativeWorkBuilder
import amf.metadata.domain.CreativeWorkModel.{Url, Description}

/**
  * Creative work internal model
  */
case class CreativeWork(fields: Fields) extends DomainElement {

  override type This = CreativeWork

  val url: String         = fields get Url
  val description: String = fields get Description

  def canEqual(other: Any): Boolean = other.isInstanceOf[CreativeWork]

  override def equals(other: Any): Boolean = other match {
    case that: CreativeWork =>
      (that canEqual this) &&
        url == that.url &&
        description == that.description
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(url, description)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"CreativeWork($url, $description)"

  override def toBuilder: CreativeWorkBuilder = CreativeWorkBuilder(fields)
}
