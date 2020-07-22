package amf.plugins.features.validation

import java.io.{PrintWriter, StringWriter, Writer => JavaWriter}

import amf.core.rdf._
import amf.core.vocabulary.Namespace
import org.apache.jena.graph.Graph
import org.apache.jena.rdf.model.{AnonId, Model, ModelFactory, Resource}
import org.apache.jena.riot._
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.sparql.util.Context
import org.mulesoft.common.io.Output

class JenaRdfModel(val model: Model = ModelFactory.createDefaultModel()) extends RdfModel {

  override def nextAnonId(): String = synchronized {
    "_:" + model.createResource().getId.toString
  }

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
    nodesCache = nodesCache - subject
    model.add(
      model.createStatement(
        checkAnon(subject),
        model.createProperty(predicate),
        checkAnon(objResource)
      )
    )
    this
  }

  override def addTriple(subject: String,
                         predicate: String,
                         objLiteralValue: String,
                         objLiteralType: Option[String]): RdfModel = {
    nodesCache = nodesCache - subject
    model.add(
      model.createStatement(
        checkAnon(subject),
        model.createProperty(predicate),
        objLiteralType match {
          case Some(typeId) => model.createTypedLiteral(objLiteralValue, typeId)
          case None         => model.createLiteral(objLiteralValue)
        }
      )
    )
    this
  }

  protected def checkAnon(s: String): Resource = {
    if (s.startsWith("_:")) {
      model.createResource(new AnonId(s.replace("_:", "")))
    } else {
      model.createResource(s)
    }
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
      case None =>
        val node = model.getResource(uri)
        if (node.isResource) {
          val res                = node.asResource()
          val id                 = res.getURI
          var resourceProperties = Map[String, Seq[PropertyObject]]()
          var resourceClasses    = Seq[String]()
          val properties         = res.listProperties()

          while (properties.hasNext) {
            val statement = properties.nextStatement()
            val predicate = statement.getPredicate.getURI
            val oldProps  = resourceProperties.getOrElse(predicate, Nil)
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
                                                              ))
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
  override def load(mediaType: String, text: String): Unit = {
    val parser = RDFParser.create().fromString(text)
    mediaType match {
      case "application/ld+json" | "application/json" =>
        parser.lang(RDFLanguages.JSONLD)
      case "text/n3" | "text/rdf+n3" =>
        parser.lang(RDFLanguages.N3)
      case "application/x-turtle" | "text/turtle" =>
        parser.lang(RDFLanguages.TURTLE)
      case "text/plain" =>
        parser.lang(RDFLanguages.NTRIPLES)
      case "application/rdf+xml" =>
        parser.lang(RDFLanguages.RDFXML)
      case _ =>
        throw new Exception(s"Unsupported RDF media type $mediaType")
    }
    parser.parse(model)
  }

  /**
    * Write model as a String representation
    *
    * @param mediaType
    * @return
    */
  override def serializeString(mediaType: String): Option[String] = {
    val writer = new StringWriter()
    val format = formatForMediaType(mediaType)
    RDFDataMgr.write(writer, model, format)
    Some(writer.toString)
  }

  override def serializeWriter[W: Output](mediaType: String, writer: W): Option[W] = {

    val format      = formatForMediaType(mediaType)
    val graphWriter = RDFWriterRegistry.getWriterGraphFactory(format).create(format)
    val modelGraph  = model.getGraph
    writer match {
      case w: JavaWriter =>
        graphWriter.write(w, modelGraph, RiotLib.prefixMap(modelGraph), "", new Context())
        Some(writer)
      case _ => None
    }
  }

  private def write(graphWriter: WriterGraphRIOT, modelGraph: Graph, writer: JavaWriter): Unit =
    graphWriter.write(writer, modelGraph, RiotLib.prefixMap(modelGraph), "", new Context())

  protected def formatForMediaType(mediaType: String) = {
    mediaType match {
      case "application/ld+json" =>
        RDFFormat.JSONLD_EXPAND_FLAT // flatten and without context
      case "text/n3" | "text/rdf+n3" =>
        RDFFormat.NT
      case "application/x-turtle" | "text/turtle" =>
        RDFFormat.TURTLE
      case "text/plain" =>
        RDFFormat.NTRIPLES
      case "application/rdf+xml" =>
        RDFFormat.RDFXML
      case _ =>
        throw new Exception(s"Unsupported RDF media type $mediaType")
    }
  }
}
