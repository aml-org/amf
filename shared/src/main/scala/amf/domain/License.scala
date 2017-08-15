package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.LicenseModel.{Name, Url}

/**
  * License internal model
  */
case class License(fields: Fields, annotations: Annotations) extends DomainElement {

  val url: String  = fields(Url)
  val name: String = fields(Name)

  def withUrl(url: String): this.type   = set(Url, url)
  def withName(name: String): this.type = set(Name, name)
}

object License {
  def apply(ast: AMFAST): License = apply(Fields(), Annotations(ast))

  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): License =
    new License(fields, annotations)
}
