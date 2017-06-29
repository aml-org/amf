package amf.remote

import amf.remote.Mimes._

/**
  * Syntax
  */
object Syntax {

  sealed trait Syntax

  case object Yaml extends Syntax
  case object Json extends Syntax

  /** Attempt to resolve [[Syntax]] from [[Mimes]]. */
  def unapply(mime: Option[String]): Option[Syntax] = mime match {
    case Some(`TEXT/YAML`) | Some(`TEXT/X-YAML`) | Some(`APPLICATION/YAML`) | Some(`APPLICATION/X-YAML`) | Some(
          `APPLICATION/RAML+YAML`) | Some(`APPLICATION/OPENAPI+YAML`) | Some(`APPLICATION/SWAGGER+YAML`) =>
      Some(Yaml)
    case Some(`APPLICATION/JSON`) | Some(`APPLICATION/RAML+JSON`) | Some(`APPLICATION/OPENAPI+JSON`) | Some(
          `APPLICATION/SWAGGER+JSON`) =>
      Some(Json)
    case _ => None
  }
}
