package amf.apicontract.internal.spec.async

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.core.internal.remote.{AsyncApi20, Spec}
import org.yaml.model.YMap

/** */
sealed case class AsyncHeader private (key: String, value: String, spec: Spec) {
  def tuple: (String, String) = (key, value)
}

object AsyncHeader {

  val async = "asyncapi"

  object Async20Header extends AsyncHeader(async, "2.0.0", AsyncApi20)
  object Async21Header extends AsyncHeader(async, "2.1.0", AsyncApi21)
  object Async22Header extends AsyncHeader(async, "2.2.0", AsyncApi22)
  object Async23Header extends AsyncHeader(async, "2.3.0", AsyncApi23)
  object Async24Header extends AsyncHeader(async, "2.4.0", AsyncApi24)
  object Async25Header extends AsyncHeader(async, "2.5.0", AsyncApi25)
  object Async26Header extends AsyncHeader(async, "2.6.0", AsyncApi26)

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
      case Async21Header.value => Some(Async21Header)
      case Async22Header.value => Some(Async22Header)
      case Async23Header.value => Some(Async23Header)
      case Async24Header.value => Some(Async24Header)
      case Async25Header.value => Some(Async25Header)
      case Async26Header.value => Some(Async26Header)
      case _                   => None
    }
  }
}
