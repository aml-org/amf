package amf.cli.internal.`export`

import amf.core.internal.metamodel.Type.{Any, ArrayLike, Scalar, SortedArray}
import amf.core.internal.metamodel.{Field, Obj, Type}

object ModelTraverser {

  def traverse(units: List[Obj], context: Context): List[Model] = units.flatMap(u => traverse(u, context))

  def traverse(obj: Obj, context: Context): List[Model] = {
    val name = getCleanedObjName(obj)
    if (context.isProcessed(name)) List()
    else {
      context.addProcessed(name)
      val attributes = makeAttributes(obj)
      val model      = Model(name, obj, attributes)
      val toTraverse: List[Model] =
        attributes.map(t => t._2).flatMap(v => v.linkedObj).flatMap(d => traverse(d, context))
      List(model) ::: toTraverse
    }
  }

  private def getCleanedObjName(obj: Type) = obj.getClass.getSimpleName.replace("$", "")

  def makeAttributes(obj: Obj): List[(String, Attribute)] = obj.fields.map(f => attributeTuple(f))

  def attributeTuple(f: Field): (String, Attribute) =
    (f.value.name, createAttribute(f.`type`, f.doc.description, f.toString))

  def createAttribute(t: Type, doc: String, namespace: String): Attribute = t match {
    case Scalar(id)         => AttributeType(id, t, doc, namespace: String)
    case Any                => AttributeType("Any", t, doc, namespace: String)
    case ArrayLike(element) => ArrayAttribute(t.isInstanceOf[SortedArray], createAttribute(element, doc, namespace))
    case obj: Obj           => TraversableAttribute(getCleanedObjName(t), obj, doc, namespace: String)
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
  def linkedObj: Option[Obj]
  def docDescription: String
  def namespace: String
}

case class ArrayAttribute(sorted: Boolean, attribute: Attribute) extends Attribute("") {
  override def toString: String       = attribute.toString
  override def linkedObj: Option[Obj] = attribute.linkedObj
  def isTraversable: Boolean          = linkedObj.nonEmpty
  def namespace: String               = attribute.namespace
  override def docDescription: String = attribute.docDescription
}

case class TraversableAttribute(override val name: String, obj: Obj, docDescription: String, namespace: String)
    extends Attribute(name) {
  override def linkedObj: Option[Obj] = Some(obj)
}

case class AttributeType(override val name: String, underlyingType: Type, doc: String, namespace: String)
    extends Attribute(name) {
  override def linkedObj: Option[Obj] = None

  override def docDescription: String = doc
}
