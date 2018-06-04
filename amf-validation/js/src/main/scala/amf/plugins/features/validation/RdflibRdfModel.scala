package amf.plugins.features.validation

import amf.core.rdf.RdfModel

import scala.scalajs.js

class RdflibRdfModel() extends RdfModel {

  val rdf: js.Dynamic = if (js.isUndefined(js.Dynamic.global.SHACLValidator)) {
    throw new Exception("Cannot find global SHACLValidator object")
  }  else {
    js.Dynamic.global.SHACLValidator.`$rdf`
  }

  val model: js.Dynamic = rdf.graph()

  override def addTriple(subject: String, predicate: String, objResource: String): RdfModel = {
    val s = rdf.namedNode(subject)
    val p = rdf.namedNode(predicate)
    val o = rdf.namedNode(objResource)

    model.add(s, p, o)

    this
  }

  override def addTriple(subject: String, predicate: String, objLiteralValue: String, objLiteralType: Option[String]): RdfModel = {
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
}
