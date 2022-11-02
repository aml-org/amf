package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.ModelDoc
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.SemanticContext
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDObject}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonPath}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleNodes
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class JsonLDObjectElementBuilder(location: SourceLocation, key: String, base: String, path: JsonPath)
    extends JsonLDElementBuilder(location, path) {
  override type THIS = JsonLDObjectElementBuilder
  type KEY           = String
  type TERM          = String
  val properties: mutable.Map[KEY, JsonLDPropertyBuilder] = mutable.Map()
  val termIndex: mutable.LinkedHashMap[TERM, KEY]         = mutable.LinkedHashMap()
  val termExtends: mutable.LinkedHashMap[TERM, TERM]      = mutable.LinkedHashMap()
  val classTerms: ListBuffer[String]                      = ListBuffer()

  def +(property: JsonLDPropertyBuilder): JsonLDObjectElementBuilder = {

    addProperty(property)
    addTerm(property)
    this
  }

  override def merge(
      other: JsonLDObjectElementBuilder
  )(implicit ctx: JsonLDParserContext): JsonLDObjectElementBuilder = {
    super.merge(other)

    other.classTerms.foreach { t => if (!classTerms.contains(t)) classTerms.prepend(t) }
    other.properties.foreach { t =>
      if (properties.contains(t._2.key)) mergeAndReplaceProperty(properties(t._2.key), t._2)
      else this + t._2
    }
    this
  }

  def mergeAndReplaceProperty(current: JsonLDPropertyBuilder, other: JsonLDPropertyBuilder)(implicit
      ctx: JsonLDParserContext
  ): Any = {
    val merged: JsonLDElementBuilder = current.element match {
      case _: JsonLDErrorBuilder                               => other.element
      case _ if other.element.isInstanceOf[JsonLDErrorBuilder] => current.element
      case obj: JsonLDObjectElementBuilder if other.element.isInstanceOf[JsonLDObjectElementBuilder] =>
        obj.merge(other.element.asInstanceOf[JsonLDObjectElementBuilder])
      case array: JsonLDArrayElementBuilder if other.element.isInstanceOf[JsonLDArrayElementBuilder] =>
        array.merge(other.element.asInstanceOf[JsonLDArrayElementBuilder])
      case scalar: JsonLDScalarElementBuilder if other.element.isInstanceOf[JsonLDScalarElementBuilder] =>
        scalar.merge(other.element.asInstanceOf[JsonLDScalarElementBuilder])
      case other =>
        ctx.violation(IncompatibleNodes, "", IncompatibleNodes.message, current.location)
        other
    }

    properties.remove(current.key)
    this + other.copy(element = merged)
    this
  }

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = {
    val obj = buildObj(ctxBuilder)
    (obj, obj.meta)
  }

  private def buildObj(ctxBuilder: EntityContextBuilder): JsonLDObject = {
    val fields = termIndex.map { case (term, key) =>
      val currentBuilder: JsonLDPropertyBuilder = properties(key)
      val (element, elementType)                = currentBuilder.element.build(ctxBuilder)
      createField(currentBuilder, elementType, term) -> element
    }

    val entityModel = new JsonLDEntityModel(classTerms.map(ValueType.apply).toList, fields.keys.toList, path)
    ctxBuilder + entityModel
    val dObject = new JsonLDObject(Fields(), Annotations(), entityModel, path.last)
    fields.foreach { f => dObject.set(f._1, f._2) }
    dObject
  }

  private def createField(builder: JsonLDPropertyBuilder, elementType: Type, term: String): Field = {
    val finalTerm = builder.element.getOverriddenTerm.getOrElse(term)
    val finalType = builder.element.getOverriddenType.getOrElse(elementType)
    Field(finalType, ValueType(finalTerm), ModelDoc(displayName = builder.key))
  }

  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDObjectElementBuilder]

  private def addProperty(property: JsonLDPropertyBuilder) = properties += property.key -> property

  private def addTerm(property: JsonLDPropertyBuilder): Unit = {
    val hasToExtend = hasDifferentKeyForTerm(property)
    if (hasToExtend) {
      solveTermClash(property)
    } else addTerm(property.term, property.key)
  }

  private def hasDifferentKeyForTerm(property: JsonLDPropertyBuilder) = {
    termIndex.contains(property.term) && !termIndex.get(property.term).contains(property.key)
  }

  private def solveTermClash(property: JsonLDPropertyBuilder) = {
    val createdTerms = replaceTermsWithPath(property)
    createdTerms.foreach { term => addTermExtension(term, property.term) }
  }

  private def addTerm(term: TERM, key: KEY): Unit = termIndex += term -> key

  private def addTermExtension(term: TERM, key: TERM) = termExtends += term -> key

  private def replaceTermsWithPath(property: JsonLDPropertyBuilder): List[String] = {
    val anotherTerm = termIndex.get(property.term).map { key =>
      termIndex.remove(property.term)
      addTermForProperty(properties(key))
    }
    val aTerm = addTermForProperty(property)
    List(aTerm) ++ anotherTerm.toList
  }

  private def addTermForProperty(property: JsonLDPropertyBuilder): String = {
    val nextTerm = computeTerm(property)
    addTerm(nextTerm, property.key)
    nextTerm
  }
  private def computeTerm(property: JsonLDPropertyBuilder) = base + property.path.toString
}

object JsonLDObjectElementBuilder {
  def empty(key: String, path: JsonPath) =
    new JsonLDObjectElementBuilder(SourceLocation.Unknown, key, SemanticContext.baseIri, path)
}

case class JsonLDPropertyBuilder(
    term: String,
    key: String,
    father: Option[String],
    element: JsonLDElementBuilder,
    path: JsonPath,
    location: SourceLocation
)
