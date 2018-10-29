package amf.core.vocabulary

import scala.collection.mutable

/**
  * Namespaces
  */
case class Namespace(base: String) {
  def +(id: String): ValueType = ValueType(this, id)
}

object Namespace {

  val Document = Namespace("http://a.ml/vocabularies/document#")

  val Http = Namespace("http://a.ml/vocabularies/http#")

  val Security = Namespace("http://a.ml/vocabularies/security#")

  val Shapes = Namespace("http://a.ml/vocabularies/shapes#")

  val Data = Namespace("http://a.ml/vocabularies/data#")

  val SourceMaps = Namespace("http://a.ml/vocabularies/document-source-maps#")

  val Shacl = Namespace("http://www.w3.org/ns/shacl#")

  val Schema = Namespace("http://schema.org/")

  val Hydra = Namespace("http://www.w3.org/ns/hydra/core#")

  val Xsd = Namespace("http://www.w3.org/2001/XMLSchema#")

  val AnonShapes = Namespace("http://a.ml/vocabularies/shapes/anon#")

  val Rdf = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")

  // To build full URIs without namespace
  val WihtoutNamespace = Namespace("")

  val Meta = Namespace("http://a.ml/vocabularies/meta#")

  val Owl = Namespace("http://www.w3.org/2002/07/owl#")

  val Rdfs = Namespace("http://www.w3.org/2000/01/rdf-schema#")

  val AmfParser = Namespace("http://a.ml/vocabularies/amf/parser#")

  val AmfResolution = Namespace("http://a.ml/vocabularies/amf/resolution#")

  val AmfValidation = Namespace("http://a.ml/vocabularies/amf/validation#")

  val AmfRender = Namespace("http://a.ml/vocabularies/amf/render#")

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

  def compactAndCollect(uri: String, prefixes: mutable.Map[String, String]): String = {
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

  def find(uri: String): Option[Namespace] = uri match {
    case "http://a.ml/vocabularies/document" => Some(Document)

    case "http://a.ml/vocabularies/http" => Some(Http)

    case "http://a.ml/vocabularies/security" => Some(Security)

    case "http://a.ml/vocabularies/shapes" => Some(Shapes)

    case "http://a.ml/vocabularies/data" => Some(Data)

    case "http://a.ml/vocabularies/document-source-maps" => Some(SourceMaps)

    case "http://www.w3.org/ns/shacl" => Some(Shacl)

    case "http://schema.org/" => Some(Schema)

    case "http://www.w3.org/ns/hydra/core" => Some(Hydra)

    case "http://www.w3.org/2001/XMLSchema" => Some(Xsd)

    case "http://a.ml/vocabularies/shapes/anon" => Some(AnonShapes)

    case "http://www.w3.org/1999/02/22-rdf-syntax-ns" => Some(Rdf)

    // To build full URIs without namespace
    case "" => Some(WihtoutNamespace)

    case "http://a.ml/vocabularies/meta" => Some(Meta)

    case "http://www.w3.org/2002/07/owl" => Some(Owl)

    case "http://www.w3.org/2000/01/rdf-schema" => Some(Rdfs)

    case "http://a.ml/vocabularies/amf/parser" => Some(AmfParser)

    case _ => None
  }

  private def resolve(prefix: String): Option[Namespace] = ns.get(prefix)

  object XsdTypes {
    val xsdString: ValueType  = Namespace.Xsd + "string"
    val xsdInteger: ValueType = Namespace.Xsd + "integer"
    val xsdFloat: ValueType   = Namespace.Xsd + "float"
    val amlNumber: ValueType  = Namespace.Shapes + "number"
    val amlLink: ValueType    = Namespace.Shapes + "link"
    val xsdDouble: ValueType  = Namespace.Xsd + "double"
    val xsdBoolean: ValueType = Namespace.Xsd + "boolean"
    val xsdNil: ValueType     = Namespace.Xsd + "nil"
    val xsdUri: ValueType     = Namespace.Xsd + "anyUri"
    val xsdAnyType: ValueType = Namespace.Xsd + "anyType"
    val amlAnyNode: ValueType = Namespace.Meta + "anyNode"
  }

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
  def apply(iri: String): ValueType =
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
