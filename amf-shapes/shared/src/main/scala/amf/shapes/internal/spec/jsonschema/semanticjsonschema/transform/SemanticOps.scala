package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

object SemanticOps {

  def getPrefixes(iris: Set[String]): Set[String] = {
    iris.map(_.split(":")).flatMap {
      case Array(prefix: String, _: String) => Some(prefix)
      case _                                => None
    }
  }

  def findPrefix(iri: String, prefix: Map[String, String]): Option[String] = {
    prefix
      .find {
        case (_, value) => iri.startsWith(value)
      }
      .map(_._1)
  }

  def expandIri(iri: String, prefixes: Map[String, String], default: Option[String]): String = {
    if (isCompactIri(iri)) {
      val Array(prefix, postfix @ _ *) = iri.split(":")
      if (prefix.isEmpty && default.nonEmpty) s"${default.get}$postfix"
      else
        prefixes
          .get(prefix)
          .map(prefixIri => s"$prefixIri${postfix.mkString(":")}")
          .getOrElse(iri)
    } else iri
  }

  private def isCompactIri(iri: String) = {
    iri.contains(":") && !iri.contains("://")
  }
}
