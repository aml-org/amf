package amf.core.vocabulary

import scala.collection.mutable

/**
  * Namespaces
  */
case class Namespace(base: String) {
  def +(id: String): ValueType = ValueType(this, id)
}

object Namespace {

  val Document = Namespace("http://raml.org/vocabularies/document#")

  val Http = Namespace("http://raml.org/vocabularies/http#")

  val Security = Namespace("http://raml.org/vocabularies/security#")

  val Shapes = Namespace("http://raml.org/vocabularies/shapes#")

  val Data = Namespace("http://raml.org/vocabularies/data#")

  val SourceMaps = Namespace("http://raml.org/vocabularies/document-source-maps#")

  val Shacl = Namespace("http://www.w3.org/ns/shacl#")

  val Schema = Namespace("http://schema.org/")

  val Hydra = Namespace("http://www.w3.org/ns/hydra/core#")

  val Xsd = Namespace("http://www.w3.org/2001/XMLSchema#")

  val AnonShapes = Namespace("http://raml.org/vocabularies/shapes/anon#")

  val Rdf = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")

  // To build full URIs without namespace
  val WihtoutNamespace = Namespace("")

  val Meta = Namespace("http://raml.org/vocabularies/meta#")

  val Owl = Namespace("http://www.w3.org/2002/07/owl#")

  val Rdfs = Namespace("http://www.w3.org/2000/01/rdf-schema#")

  val AmfParser = Namespace("http://raml.org/vocabularies/amf/parser#")

  val AmfValidation = Namespace("http://raml.org/vocabularies/amf/validation#")

  val ns = mutable.HashMap(
    "rdf"         -> Rdf,
    "sh"          -> Shacl,
    "shacl"       -> Shacl,
    "security"    -> Security,
    "schema-org"  -> Schema,
    "schema"      -> Schema,
    "raml-http"   -> Http,
    "http"        -> Http,
    "raml-doc"    -> Document,
    "doc"         -> Document,
    "xsd"         -> Xsd,
    "amf-parser"  -> AmfParser,
    "hydra"       -> Hydra,
    "raml-shapes" -> Shapes,
    "data"        -> Data,
    "sourcemaps"  -> SourceMaps,
    "meta"        -> Meta,
    "owl"         -> Owl,
    "rdfs"        -> Rdfs
  )

  def uri(s: String): ValueType = {
    if (s.indexOf(":") > -1) {
      expand(s)
    } else {
      ns.values.find(n => s.indexOf(n.base) == 0) match {
        case Some(foundNs) => ValueType(foundNs, s.split(foundNs.base).last)
        case _             => ValueType(s)
      }
    }
  }

  def registerNamespace(alias: String, prefix: String): Option[Namespace] = ns.put(alias, Namespace(prefix))

  def expand(uri: String): ValueType = {
    if (uri.startsWith("http://")) { // we have http: as  a valid prefix, we need to disambiguate
      ValueType(uri)
    } else {
      uri.split(":") match {
        case Array(prefix, postfix) =>
          resolve(prefix) match {
            case Some(n) => ValueType(n, postfix)
            case _       => ValueType(uri)
          }
        case _ => ValueType(uri)
      }
    }
  }

  def compact(uri: String): String = {
    ns.find {
      case (_, namespace) =>
        uri.indexOf(namespace.base) == 0
    } match {
      case Some((prefix, namespace)) =>
        prefix ++ uri.replace(namespace.base, ":")
      case None => uri
    }
  }

  def compactAndCollect(uri: String, prefixes: mutable.Map[String, String]) = {
    ns.find {
      case (_, namespace) =>
        uri.indexOf(namespace.base) == 0
    } match {
      case Some((prefix, namespace)) =>
        prefixes.put(prefix, namespace.base)
        prefix ++ uri.replace(namespace.base, ":")
      case None => uri
    }
  }

  def find(uri: String): Namespace = uri match {
    case "http://raml.org/vocabularies/document" => Document

    case "http://raml.org/vocabularies/http" => Http

    case "http://raml.org/vocabularies/security" => Security

    case "http://raml.org/vocabularies/shapes" => Shapes

    case "http://raml.org/vocabularies/data" => Data

    case "http://raml.org/vocabularies/document-source-maps" => SourceMaps

    case "http://www.w3.org/ns/shacl" => Shacl

    case "http://schema.org/" => Schema

    case "http://www.w3.org/ns/hydra/core" => Hydra

    case "http://www.w3.org/2001/XMLSchema" => Xsd

    case "http://raml.org/vocabularies/shapes/anon" => AnonShapes

    case "http://www.w3.org/1999/02/22-rdf-syntax-ns" => Rdf

    // To build full URIs without namespace
    case "" => WihtoutNamespace

    case "http://raml.org/vocabularies/meta" => Meta

    case "http://www.w3.org/2002/07/owl" => Owl

    case "http://www.w3.org/2000/01/rdf-schema" => Rdfs

    case "http://raml.org/vocabularies/amf/parser" => AmfParser
  }

  private def resolve(prefix: String): Option[Namespace] = ns.get(prefix)

}

/** Value type. */
case class ValueType(ns: Namespace, name: String) {
  def iri(): String = ns.base + name
}

class UriType(id: String) extends ValueType(Namespace.Document, "") {
  override def iri(): String = id
}

object ValueType {
  def apply(ns: Namespace, name: String) = new ValueType(ns, name)
  def apply(iri: String) =
    if (iri.contains("#")) {
      val pair = iri.split("#")
      val name = pair.last
      val ns   = pair.head + "#"
      new ValueType(Namespace(ns), name)
    } else if (iri.replace("://", "_").contains("/")) {
      val name = iri.split("/").last
      val ns   = iri.replace(name, "")
      new ValueType(Namespace(ns), name)
    } else {
      new ValueType(Namespace(iri), "")
    }
}
