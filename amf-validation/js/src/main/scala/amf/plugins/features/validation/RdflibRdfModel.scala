package amf.plugins.features.validation

import amf.core.rdf._
import amf.core.vocabulary.Namespace
import org.mulesoft.common.io.Output

import scala.scalajs.js

object RDF {
  lazy val instance: js.Dynamic = if (js.isUndefined(js.Dynamic.global.GlobalSHACLValidator)) {
    throw new Exception("Cannot find global SHACLValidator object")
  } else {
    js.Dynamic.global.GlobalSHACLValidator.`$rdf`
  }
}

object JSONLD {
  lazy val instance: js.Dynamic = if (js.isUndefined(js.Dynamic.global.SHACLValidator)) {
    throw new Exception("Cannot find global SHACLValidator object")
  } else {
    js.Dynamic.global.SHACLValidator.jsonld
  }
}

class RdflibRdfModel(val model: js.Dynamic = RDF.instance.graph()) extends RdfModel {

  lazy val rdf: js.Dynamic    = RDF.instance
  lazy val jsonld: js.Dynamic = JSONLD.instance

  override def nextAnonId(): String = synchronized {
    rdf.blankNode().toString
  }

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
    nodesCache = nodesCache - subject
    val s = checkAnon(subject)
    val p = rdf.namedNode(predicate)
    val o = checkAnon(objResource)

    model.add(s, p, o)

    this
  }

  override def addTriple(subject: String,
                         predicate: String,
                         objLiteralValue: String,
                         objLiteralType: Option[String]): RdfModel = {
    nodesCache = nodesCache - subject
    val s = checkAnon(subject)
    val p = rdf.namedNode(predicate)
    val o = objLiteralType match {
      case Some(literalType) =>
        val dt = rdf.namedNode(literalType)
        rdf.literal(objLiteralValue, dt)
      case _ => rdf.literal(objLiteralValue)
    }

    model.add(s, p, o)

    this
  }

  protected def checkAnon(s: String): js.Any = {
    if (s.startsWith("_:n")) {
      try {
        val idString = s.replace("_:n", "")
        val id       = Integer.parseInt(idString)
        val node     = rdf.blankNode(idString)
        node.id = id
        node
      } catch {
        case _: Throwable => rdf.namedNode(s)
      }
    } else {
      rdf.namedNode(s)
    }
  }

  override def toN3(): String = {
    (model.toNTSync() + "")
  }

  override def native(): Any = model

  var nodesCache: Map[String, Node] = Map()

  override def findNode(uri: String): Option[Node] = {
    nodesCache.get(uri) match {
      case Some(node) => Some(node)
      case _ =>
        val statements = model.getQuads(uri).asInstanceOf[js.Array[js.Dynamic]]
        if (statements.isEmpty) {
          None
        } else {
          var resourceProperties = Map[String, Seq[PropertyObject]]()
          var resourceClasses    = Seq[String]()

          statements
            .sortWith((t1, t2) =>
              (t1.predicate.uri.asInstanceOf[String] compareTo t2.predicate.uri.asInstanceOf[String]) > 0)
            .foreach { statement =>
              val property = statement.predicate.uri.asInstanceOf[String]

              val obj      = statement.`object`
              val oldProps = resourceProperties.getOrElse(property, Nil)

              if (property == (Namespace.Rdf + "type").iri()) {
                resourceClasses ++= Seq(obj.uri.asInstanceOf[String])
              } else if (obj.termType.asInstanceOf[String] == "Literal") {

                resourceProperties = resourceProperties.updated(
                  property,
                  oldProps ++ Seq(
                    Literal(
                      value = s"${obj.value}",
                      literalType = if (Option(obj.datatype).isDefined) {
                        Some(obj.datatype.uri.asInstanceOf[String])
                      } else {
                        None
                      }
                    )
                  )
                )

              } else {
                resourceProperties =
                  resourceProperties.updated(property,
                                             oldProps ++ Seq(
                                               Uri(
                                                 value = s"${Option(obj.uri).getOrElse(obj.toCanonical())}"
                                               )
                                             ))
              }
            }

          val newNode = Node(uri, resourceClasses, resourceProperties)
          nodesCache = nodesCache.updated(uri, newNode)
          Some(newNode)
        }
    }

  }
  override def load(mediaType: String, text: String): Unit = {
    var effectiveMediaType = if (mediaType == "application/json") "application/ld+json" else mediaType
    rdf.parse(text, model, "", effectiveMediaType)
  }

  /**
    * Write model as a String representation
    *
    * @param mediaType
    * @return
    */
  override def serializeString(mediaType: String): Option[String] = {
    throw new Exception("Sync rdf serialization not supported yet")
  }

  /**
    * Write model using a writer
    *
    * @param mediaType
    * @param writer writer where to send the representation
    * @return
    */
  override def serializeWriter[W: Output](mediaType: String, writer: W): Option[W] = {
    throw new Exception("Sync rdf serialization to writer not supported yet")
  }
}
