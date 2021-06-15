package amf.plugins.document.apicontract.parser

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import org.yaml.model.YMap

/**
  *
  */
class AsyncHeader(val key: String, val value: String) {
  def tuple: (String, String) = (key, value)
}

object AsyncHeader {

  val async = "asyncapi"

  object Async20Header extends AsyncHeader(async, "2.0.0")

  def apply(root: Root): Option[AsyncHeader] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) =>
            map
              .key(async)
              .flatMap(extension => AsyncHeader(extension.value.toOption[String].getOrElse("")))
          case Left(_) => None
        }
      case _ => None
    }

  def apply(text: String): Option[AsyncHeader] = {
    text match {
      case Async20Header.value => Some(Async20Header)
      case _                   => None
    }
  }
}
