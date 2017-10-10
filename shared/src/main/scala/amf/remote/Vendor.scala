package amf.remote

import amf.remote.Syntax.{Json, Syntax, Yaml}

/**
  * Created by pedro.colunga on 10/9/17.
  */
object Vendor {
  def unapply(name: String): Option[Vendor] = {
    name match {
      case "raml" => Some(Raml)
      case "oas"  => Some(Oas)
      case "amf"  => Some(Amf)
      case _      => None
    }
  }
}

sealed trait Vendor {
  val name: String
  val defaultSyntax: Syntax
}

object Raml extends Vendor {
  override val name: String          = "raml"
  override val defaultSyntax: Syntax = Yaml
}

object Oas extends Vendor {
  override val name: String          = "oas"
  override val defaultSyntax: Syntax = Json
}

object Amf extends Vendor {
  override val name: String          = "amf"
  override val defaultSyntax: Syntax = Json
}
