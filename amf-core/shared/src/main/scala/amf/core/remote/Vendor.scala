package amf.core.remote

import amf.core.remote.Syntax.{Json, PlainText, Syntax, Yaml}

/**
  * Created by pedro.colunga on 10/9/17.
  */
object Vendor {
  def unapply(name: String): Option[Vendor] = {
    name match {
      case "raml 1.0" => Some(Raml10)
      case "raml 0.8" => Some(Raml08)
      case "raml"     => Some(Raml) // todo remove later
      case "oas"      => Some(Oas)
      case "amf"      => Some(Amf)
      case "payload"  => Some(Payload)
      case _          => None
    }
  }
}

sealed trait Vendor {
  val name: String
  val defaultSyntax: Syntax
}

trait Raml extends Vendor {
  def version: String
  override val name: String          = ("raml " + version).trim
  override val defaultSyntax: Syntax = Yaml

  override def toString: String = name.trim

}

object Raml extends Raml {
  override def version: String = ""
}

object Raml10 extends Raml {
  override def version: String = "1.0"
}

object Raml08 extends Raml {
  override def version: String = "0.8"
}

object Oas extends Vendor {
  override val name: String          = "oas"
  override val defaultSyntax: Syntax = Json

  override def toString: String = name
}

object Amf extends Vendor {
  override val name: String          = "amf"
  override val defaultSyntax: Syntax = Json
}

object Unknown extends Vendor {
  override val name: String          = "external"
  override val defaultSyntax: Syntax = PlainText
}

object Payload extends Vendor {
  override val name: String          = "payload"
  override val defaultSyntax: Syntax = Json
}

object Extension extends Vendor {
  override val name: String          = "extension"
  override val defaultSyntax: Syntax = Yaml
}
