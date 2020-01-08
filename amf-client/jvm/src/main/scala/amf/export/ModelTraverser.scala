package amf.`export`

import amf.core.metamodel.{Field, Obj, Type}
import amf.core.metamodel.Type.{Any, ArrayLike, Scalar}

object ModelTraverser {

  def traverse(units: List[Obj], context: Context): List[Model] = {
    units.flatMap(u => traverse(u, context))
  }

  def traverse(unit: Obj, context: Context): List[Model] = {
    val name = unit.getClass.getSimpleName.replace("$", "")
    if (context.isProcessed(name)) List()
    else {
      context.addProcessed(name)
      val att                     = attributes(unit)
      val model                   = Model(name, unit, att)
      val toTraverse: List[Model] = att.map(t => t._2).flatMap(v => v.baseUnit).flatMap(d => traverse(d, context))
      List(model) ::: toTraverse
    }
  }

  def attributes(obj: Obj): List[(String, Attribute)] = {
    obj.fields.map(f => attributeTuple(f))
  }

  def attributeTuple(f: Field): (String, Attribute) =
    (f.value.name, createAttribute(f.`type`, f.doc.description, f.toString))

  def createAttribute(t: Type, doc: String, namespace: String): Attribute = t match {
    case Scalar(id)         => AttributeType(id, t, doc, namespace: String)
    case Any                => AttributeType("Any", t, doc, namespace: String)
    case ArrayLike(element) => ArrayAttribute(createAttribute(element, doc, namespace))
    case obj: Obj           => TraversableAttribute(t.getClass.getSimpleName.replace("$", ""), obj, namespace: String)
  }
}

case class Model(name: String, obj: Obj, attributes: List[(String, Attribute)])

class Context(var processed: List[String] = List()) {

  def addProcessed(model: String): Unit = {
    processed = model :: processed
  }

  def isProcessed(model: String): Boolean = {
    processed.contains(model)
  }
}

abstract class Attribute(val name: String) {
  override def toString: String = name
  def baseUnit: Option[Obj]
  def docDescription: String
  def namespace: String
}

case class ArrayAttribute(attribute: Attribute) extends Attribute("") {
  override def toString: String       = attribute.toString
  override def baseUnit: Option[Obj]  = attribute.baseUnit
  def isTraversable: Boolean          = baseUnit.nonEmpty
  def namespace: String               = attribute.namespace
  override def docDescription: String = attribute.docDescription
}

case class TraversableAttribute(override val name: String, obj: Obj, namespace: String) extends Attribute(name) {
  override def baseUnit: Option[Obj] = Some(obj)

  override def docDescription: String = obj.doc.description
}
case class AttributeType(override val name: String, underlyingType: Type, doc: String, namespace: String)
    extends Attribute(name) {
  override def baseUnit: Option[Obj] = None

  override def docDescription: String = doc
}
