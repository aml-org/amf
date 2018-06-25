package amf.plugins.features.validation

import java.io.{FileWriter, PrintWriter}

import amf.core.rdf._
import amf.core.vocabulary.Namespace
import org.apache.jena.rdf.model.Model
import org.topbraid.jenax.util.JenaUtil

class JenaRdfModel(val model: Model = JenaUtil.createMemoryModel()) extends RdfModel {

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
    nodesCache = nodesCache - subject
    model.add(
      model.createStatement(
        model.createResource(subject),
        model.createProperty(predicate),
        model.createResource(objResource)
      )
    )
    this
  }

  override def addTriple(subject: String, predicate: String, objLiteralValue: String, objLiteralType: Option[String]): RdfModel = {
    nodesCache = nodesCache - subject
    model.add(
      model.createStatement(
        model.createResource(subject),
        model.createProperty(predicate),
        objLiteralType match {
          case Some(typeId) => model.createTypedLiteral(objLiteralValue, typeId)
          case None         => model.createLiteral(objLiteralValue)
        }
      )
    )
    this
  }

  override def toN3(): String = RDFPrinter(model, "N3")

  def dump() = {
    val out = new PrintWriter("/tmp/test.n3")
    /*
    model.listStatements().toList.forEach { st =>
      if (st.getObject.isLiteral) {
        out.println(s"<${st.getSubject.getURI}> <${st.getPredicate.getURI}> '${st.getObject.asLiteral().getLexicalForm}' .")
      } else {
        out.println(s"<${st.getSubject.getURI}> <${st.getPredicate.getURI}> <${st.getObject.asResource().getURI}> .")
      }
    }
    */
    out.println(RDFPrinter(model, "N3"))
    out.close()
  }

  override def native(): Any = model

  var nodesCache: Map[String, Node] = Map()

  override def findNode(uri: String): Option[Node] = {
    nodesCache.get(uri) match {
      case Some(node) => Some(node)
      case None       =>
        val node = model.getResource(uri)
        if (node.isResource) {
          val res = node.asResource()
          val id = res.getURI
          var resourceProperties = Map[String, Seq[PropertyObject]]()
          var resourceClasses    = Seq[String]()
          val properties = res.listProperties()

          while (properties.hasNext) {
            val statement = properties.nextStatement()
            val predicate = statement.getPredicate.getURI
            val oldProps = resourceProperties.getOrElse(predicate, Nil)
            if (predicate == (Namespace.Rdf + "type").iri()) {
              resourceClasses ++= Seq(statement.getObject.asResource().getURI)
            } else if (statement.getObject.isLiteral) {
              val lit = statement.getObject.asLiteral()
              resourceProperties = resourceProperties.updated(predicate,
                oldProps ++ Seq(
                  Literal(
                    value = lit.getLexicalForm,
                    literalType = Some(lit.getDatatypeURI)
                  )
                )
              )
            } else if (statement.getObject.isResource) {
              resourceProperties = resourceProperties.updated(
                predicate,
                oldProps ++ Seq(
                  Uri(
                    value = statement.getObject.asResource().getURI
                  )
                )
              )
            }
          }

          val newNode = Node(id, resourceClasses, resourceProperties)
          nodesCache = nodesCache.updated(id, newNode)
          Some(newNode)
        } else {
          None
        }
    }
  }
}
