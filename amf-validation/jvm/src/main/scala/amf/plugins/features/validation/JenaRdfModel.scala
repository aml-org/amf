package amf.plugins.features.validation

import java.io.PrintWriter

import amf.core.rdf.RdfModel
import org.apache.jena.rdf.model.Model
import org.topbraid.jenax.util.JenaUtil

class JenaRdfModel(val model: Model = JenaUtil.createMemoryModel()) extends RdfModel {

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
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
    model.listStatements().toList.forEach { st =>
      if (st.getObject.isLiteral) {
        out.println(s"<${st.getSubject.getURI}> <${st.getPredicate.getURI}> '${st.getObject.asLiteral().getLexicalForm}' .")
      } else {
        out.println(s"<${st.getSubject.getURI}> <${st.getPredicate.getURI}> <${st.getObject.asResource().getURI}> .")
      }
    }
    //out.println(RDFPrinter(model, "JSON-LD"))
    out.close()
  }

  override def native(): Any = model
}
