package amf.plugins.features.validation

import amf.core.rdf._
import amf.core.vocabulary.Namespace

import scala.scalajs.js

object RDF {
  lazy val instance: js.Dynamic = if (js.isUndefined(js.Dynamic.global.SHACLValidator)) {
    throw new Exception("Cannot find global SHACLValidator object")
  }  else {
    js.Dynamic.global.SHACLValidator.`$rdf`
  }
}

class RdflibRdfModel(val model: js.Dynamic = RDF.instance.graph()) extends RdfModel {

  val rdf: js.Dynamic = RDF.instance

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
    nodesCache = nodesCache - subject
    val s = rdf.namedNode(subject)
    val p = rdf.namedNode(predicate)
    val o = rdf.namedNode(objResource)

    model.add(s, p, o)

    this
  }

  override def addTriple(subject: String, predicate: String, objLiteralValue: String, objLiteralType: Option[String]): RdfModel = {
    nodesCache = nodesCache - subject
    val s = rdf.namedNode(subject)
    val p = rdf.namedNode(predicate)
    val o = objLiteralType match {
      case Some(literalType) => rdf.literal(objLiteralValue, literalType)
      case _                 => rdf.literal(objLiteralValue)
    }

    model.add(s, p, o)

    this
  }

  override def toN3(): String = (model.toNT() + "").drop(1).dropRight(1)

  override def native(): Any = model

  var nodesCache: Map[String, Node] = Map()

  override def findNode(uri: String): Option[Node] = {
    nodesCache.get(uri) match {
      case Some(node) => Some(node)
      case _          =>
        val id = s"<$uri>"
        model.subjectIndex.asInstanceOf[js.Dictionary[js.Array[js.Dynamic]]].get(id) match {
          case Some(statements) =>

            var resourceProperties = Map[String, Seq[PropertyObject]]()
            var resourceClasses    = Seq[String]()


            statements.foreach { statement =>

              val property = statement.predicate.uri.asInstanceOf[String]

              val obj = statement.`object`
              val oldProps = resourceProperties.getOrElse(property, Nil)

              if (property == (Namespace.Rdf + "type").iri()) {
                resourceClasses ++= Seq(obj.uri.asInstanceOf[String])
              } else if (obj.termType.asInstanceOf[String] == "Literal") {

                resourceProperties = resourceProperties.updated(property,
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
                resourceProperties = resourceProperties.updated(property,
                  oldProps ++ Seq(
                    Uri(
                      value = s"${Option(obj.uri).getOrElse(obj.toCanonical())}"
                    )
                  )
                )
              }
            }

            val newNode = Node(uri, resourceClasses, resourceProperties)
            nodesCache = nodesCache.updated(id, newNode)
            Some(newNode)

          case None => None
        }
    }

  }
}
