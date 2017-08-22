package amf.maker

import amf.compiler.Root
import amf.domain.Annotation.SourceVendor
import amf.domain.WebApi
import amf.remote.{Oas, Raml}
import amf.spec.oas.OasSpecParser
import amf.spec.raml.RamlSpecParser

/**
  * API Documentation maker.
  */
class WebApiMaker(root: Root) extends Maker[WebApi] {

  override def make: WebApi = {
    (root.vendor match {
      case Raml => RamlSpecParser(root).parseWebApi()
      case Oas  => OasSpecParser(root).parseWebApi()
      case _    => throw new IllegalStateException(s"Invalid vendor ${root.vendor}")
    }).add(SourceVendor(root.vendor))
  }
}

object WebApiMaker {
  def apply(root: Root): WebApiMaker = new WebApiMaker(root)
}
