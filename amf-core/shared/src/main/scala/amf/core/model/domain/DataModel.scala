package amf.core.model.domain

import amf.core.annotations.{DataNodePropertiesAnnotations, LexicalInformation, ScalarType}
import amf.core.metamodel.Type.{Array, EncodedIri, Str}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.DataNodeModel._
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.templates.Variable
import amf.core.parser.{Annotations, Fields, Value}
import amf.core.resolution.VariableReplacer
import amf.core.utils._
import amf.core.vocabulary.{Namespace, ValueType}
import org.yaml.model.{YPart, YSequence}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Base class for all dynamic DataNodes
  */
abstract class DataNode(annotations: Annotations) extends DynamicDomainElement {

  def name: StrField = fields.field(Name)

  def withName(name: String): this.type = set(Name, name)

  override def adopted(parent: String): this.type = {
    if (Option(id).isEmpty) simpleAdoption(parent) else this
  }

  override def componentId: String = "/" + name.option().getOrElse("data-node").urlComponentEncoded

  /** Replace all raml variables (any name inside double chevrons -> '<<>>') with the provided values. */
  def replaceVariables(values: Set[Variable], keys: Seq[ElementTree])(reportError: (String) => Unit): DataNode

  def forceAdopted(parent: String): this.type = {
    val adoptedId = parent + "/" + name.option().map(_.urlComponentEncoded).orNull
    val newId = Option(id) match {
      case Some(oldId: String) if oldId.endsWith("/included") => adoptedId + "/included"
      case _                                                  => adoptedId
    }
    withId(newId)
  }

  override val fields: Fields = Fields()

  def cloneNode(): DataNode

  override def meta: Obj = DataNodeModel

  def lexicalPropertiesAnnotation: Option[DataNodePropertiesAnnotations] = None
}

object DataNodeOps {

  /** Adopt entire data node hierarchy. */
  def adoptTree(id: String, node: DataNode): DataNode = {
    node.forceAdopted(id)
    node match {
      case array: ArrayNode =>
        array.members.foreach(adoptTree(array.id, _))
      case obj: ObjectNode =>
        obj.properties.values.foreach(adoptTree(obj.id, _))
      case _ =>
    }
    node
  }
}

/**
  * Data records, with a list of properties
  */
class ObjectNode(override val fields: Fields, val annotations: Annotations) extends DataNode(annotations) {

  val properties
    : mutable.LinkedHashMap[String, DataNode] = mutable.LinkedHashMap() // i need to keep the order of some nodes (like params defined in traits). I could order by position at resolution time, but with this structure i avoid one more traverse.
  val propertyAnnotations: mutable.Map[String, Annotations] =
    annotations.find(classOf[DataNodePropertiesAnnotations]) match {
      case Some(ann) => mutable.HashMap() ++ ann.properties.map(t => t._1 -> Annotations(t._2))
      case _         => mutable.HashMap()
    }

  def addProperty(propertyOrUri: String, objectValue: DataNode, annotations: Annotations = Annotations()): this.type = {
    val property = ensurePlainProperty(propertyOrUri)
    objectValue.adopted(this.id)

    properties += property -> objectValue
    propertyAnnotations.get(property) match {
      case Some(ann) => annotations.foreach(a => if (!ann.contains(a.getClass)) ann += a)
      case None      => propertyAnnotations.update(property, annotations)
    }

    this
  }

  protected def ensurePlainProperty(propertyOrUri: String): String =
    if (propertyOrUri.indexOf(Namespace.Data.base) == 0) {
      propertyOrUri.replace(Namespace.Data.base, "")
    } else {
      propertyOrUri
    }

  override def dynamicFields: List[Field] =
    this.properties.keys.toSeq.sorted
      .map({ p =>
        Field(DataNodeModel, Namespace.Data + p, ModelDoc(ModelVocabularies.Data, p, ""))
      })
      .toList ++ DataNodeModel.fields

  override def dynamicType = List(ObjectNode.builderType)

  override def valueForField(f: Field): Option[Value] = {
    val maybeNode = f.value.ns match {
      case Namespace.Data => properties.get(f.value.name)
      case _              => None // this or fields.get(f)
    }
    maybeNode map { Value(_, Annotations()) }
  }

  override def replaceVariables(values: Set[Variable], keys: Seq[ElementTree])(reportError: String => Unit): DataNode = {
    properties.keys.toSeq.foreach { key =>
      val decodedKey = key.urlComponentDecoded
      val finalKey: String =
        if (decodedKey.endsWith("?")) decodedKey.substring(0, decodedKey.length - 1) else decodedKey
      val maybeTree = keys.find(_.key.equals(finalKey))

      val value = properties(key)
        .replaceVariables(values, maybeTree.map(_.subtrees).getOrElse(Nil))(
          if (decodedKey
                .endsWith("?") && maybeTree.isEmpty) // TODO review this logic
            (_: String) => Unit
          else reportError) // if its an optional node, ignore the violation of the var not implement
      properties.remove(key)
      properties += VariableReplacer.replaceVariablesInKey(decodedKey, values, reportError) -> value
    }

    propertyAnnotations.keys.foreach { key =>
      val value = propertyAnnotations(key)
      propertyAnnotations.remove(key)
      propertyAnnotations += VariableReplacer.replaceVariablesInKey(key.urlComponentDecoded, values, reportError) -> value
    }

    this
  }

  override def cloneNode(): ObjectNode = {
    val cloned = ObjectNode(annotations)

    properties.foreach {
      case (property: String, l: DataNode) =>
        cloned.properties += property          -> l.cloneNode()
        cloned.propertyAnnotations += property -> propertyAnnotations(property)
    }

    cloned
  }

  override def lexicalPropertiesAnnotation: Option[DataNodePropertiesAnnotations] = {
    val stringToInformation = propertyAnnotations.flatMap {
      case (key, ann) => ann.find(classOf[LexicalInformation]).map(l => (key, l))
    }
    if (stringToInformation.nonEmpty) Some(DataNodePropertiesAnnotations(stringToInformation.toMap)) else None
  }

}

object ObjectNode {

  val builderType: ValueType = Namespace.Data + "Object"

  def apply(): ObjectNode = apply(Annotations())

  def apply(ast: YPart): ObjectNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ObjectNode = new ObjectNode(Fields(), annotations)

}

/**
  * Scalar values with associated data type
  */
class ScalarNode(var value: String,
                 var dataType: Option[String],
                 override val fields: Fields,
                 val annotations: Annotations)
    extends DataNode(annotations) {

  val Value: Field = ScalarNodeModel.Value

  override def dynamicFields: List[Field] = List(Value) ++ DataNodeModel.fields

  override def dynamicType = List(ScalarNode.builderType)

  override def valueForField(f: Field): Option[Value] = f match {
    case Value =>
      val annotations = dataType match {
        case Some(dt) => Annotations() += ScalarType(dt)
        case None     => Annotations()
      }
      Some(amf.core.parser.Value(AmfScalar(value, annotations), Annotations()))
    case _ => None
  }

  override def replaceVariables(values: Set[Variable], keys: Seq[ElementTree])(reportError: String => Unit): DataNode = {
    VariableReplacer.replaceNodeVariables(this, values, reportError)
  }

  override def cloneNode(): ScalarNode = {
    val cloned = ScalarNode(annotations)

    cloned.value = value
    cloned.dataType = dataType

    cloned
  }
}

object ScalarNode {

  val builderType: ValueType = Namespace.Data + "Scalar"

  def apply(): ScalarNode = apply("", None)

  def apply(annotations: Annotations): ScalarNode = apply("", None, annotations)

  def apply(value: String, dataType: Option[String]): ScalarNode = apply(value, dataType, Annotations())

  def apply(value: String, dataType: Option[String], ast: YPart): ScalarNode =
    apply(value, dataType, Annotations(ast))

  def apply(value: String, dataType: Option[String], annotations: Annotations): ScalarNode =
    new ScalarNode(value, dataType, Fields(), annotations)
}

/**
  * Arrays of values
  */
class ArrayNode(override val fields: Fields, val annotations: Annotations) extends DataNode(annotations) {

  val Member: Field = ArrayNodeModel.Member

  var members: ListBuffer[DataNode] = ListBuffer()

  def addMember(member: DataNode): ListBuffer[DataNode] = members += member.adopted(this.id)

  override def dynamicFields: List[Field] = List(Member) ++ positionFields() ++ DataNodeModel.fields

  override def dynamicType = List(ArrayNode.builderType, Namespace.Rdf + "Seq")

  override def valueForField(f: Field): Option[Value] = f match {
    case Member => Some(Value(AmfArray(members), Annotations()))
    case _ if f.value.iri().startsWith((Namespace.Data + "pos").iri()) => {
      val pos    = Integer.parseInt(f.value.iri().replace((Namespace.Data + "pos").iri(), ""))
      val member = members(pos)
      Some(Value(AmfScalar(member.id), Annotations()))
    }
    case _ => None
  }

  override def replaceVariables(values: Set[Variable], keys: Seq[ElementTree])(reportError: String => Unit): DataNode = {
    members = members.map(_.replaceVariables(values, keys)(reportError))
    this
  }

  override def cloneNode(): this.type = {
    val cloned = ArrayNode(annotations)

    cloned.members = members.map(_.cloneNode())

    cloned.asInstanceOf[this.type]
  }

  def positionFields(): Seq[Field] = members.zipWithIndex.map {
    case (_, i) =>
      Field(EncodedIri, Namespace.Data + s"pos$i", ModelDoc(ModelVocabularies.Data, s"pos$i", ""))
  }
}

object ArrayNode {

  val builderType: ValueType = Namespace.Data + "Array"

  def apply(): ArrayNode = apply(Annotations())

  def apply(ast: YSequence): ArrayNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayNode = new ArrayNode(Fields(), annotations)
}

/**
  * Dynamic node representing a link to another dynamic node
  * @param alias human readable value for the link
  * @param value actual URI value for the link
  * @param fields default fields for the dynamic node
  * @param annotations deafult annotations for the dynamic node
  */
class LinkNode(var alias: String, var value: String, override val fields: Fields, val annotations: Annotations)
    extends DataNode(annotations) {

  val Value: Field                               = Field(Str, Namespace.Data + "value", ModelDoc(ModelVocabularies.Data, "value", ""))
  val Alias: Field                               = Field(Str, Namespace.Data + "alias", ModelDoc(ModelVocabularies.Data, "alias", ""))
  var linkedDomainElement: Option[DomainElement] = None

  override def dynamicFields: List[Field] = List(Value) ++ DataNodeModel.fields

  override def dynamicType = List(LinkNode.builderType)

  override def valueForField(f: Field): Option[Value] = {
    val maybeScalar = f match {
      case Value =>
        Some(AmfScalar(value, annotations))
      case Alias =>
        Some(AmfScalar(alias, annotations))
      case _ => None
    }
    maybeScalar map { amf.core.parser.Value(_, Annotations()) }
  }

  override def replaceVariables(values: Set[Variable], keys: Seq[ElementTree])(
      reportError: (String) => Unit): DataNode = this

  override def cloneNode(): LinkNode = {
    val cloned = LinkNode(annotations)

    cloned.value = value
    cloned.alias = alias
    cloned.linkedDomainElement = linkedDomainElement

    cloned
  }

  def withLinkedDomainElement(domainElement: DomainElement): LinkNode = {
    linkedDomainElement = Some(domainElement)
    this
  }
}

object LinkNode {

  val builderType: ValueType = Namespace.Data + "Link"

  def apply(): LinkNode = apply(Annotations())

  def apply(annotations: Annotations): LinkNode = apply("", "", annotations)

  def apply(alias: String, value: String): LinkNode =
    new LinkNode(alias, value, Fields(), Annotations())

  def apply(alias: String, value: String, annotations: Annotations): LinkNode =
    new LinkNode(alias, value, Fields(), annotations)
}

case class ElementTree(key: String, subtrees: Seq[ElementTree])
