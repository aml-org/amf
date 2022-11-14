package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.ModelDoc
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.SemanticContext
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDElement, JsonLDObject}
import amf.shapes.internal.domain.metamodel.jsonldschema.{JsonLDElementModel, JsonLDEntityModel}
import amf.shapes.internal.spec.jsonldschema.parser.builder.ArrayTypeComputation.computeType
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDObjectElementBuilder.{Key, Term, buildObj}
import amf.shapes.internal.spec.jsonldschema.parser.builder.ObjectPropertyMerge.mergeProperties
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonPath}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleNodes
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class JsonLDObjectElementBuilder(location: SourceLocation, key: String, base: String, path: JsonPath)
    extends JsonLDElementBuilder(location, path) {
  override type THIS = JsonLDObjectElementBuilder
  val properties: mutable.Map[Key, JsonLDPropertyBuilder]                 = mutable.Map()
  val termIndex: mutable.LinkedHashMap[Term, List[JsonLDPropertyBuilder]] = mutable.LinkedHashMap()
  val classTerms: ListBuffer[String]                                      = ListBuffer()

  def +(property: JsonLDPropertyBuilder): JsonLDObjectElementBuilder = {
    addProperty(property)
    addTerm(property)
    this
  }

  override def merge(
      other: JsonLDObjectElementBuilder
  )(implicit ctx: JsonLDParserContext): JsonLDObjectElementBuilder = {
    super.merge(other)

    addClassTerms(other)
    other.properties.foreach { case (_, builder) =>
      if (!hasBeenParsedWithSameSemantics(builder) && isSubSchemaSemanticDefinition(builder)) {
        val current = properties(builder.key)
        val merged  = mergeProperties(current, builder)
        remove(current)
        this + builder.copy(element = merged)
      }
    }
    this
  }

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = {
    val obj = buildObj(termIndex, classTerms.toList, path, ctxBuilder)
    (obj, obj.meta)
  }

  private def isSubSchemaSemanticDefinition(builder: JsonLDPropertyBuilder) = {
    properties.contains(builder.key) && !builder.hasTermWithDefaultBase
  }

  private def hasBeenParsedWithSameSemantics(property: JsonLDPropertyBuilder) = {
    termIndex.getOrElse(property.term, List.empty).contains(property)
  }

  private def addClassTerms(other: JsonLDObjectElementBuilder): Unit = {
    other.classTerms.foreach { t => if (!classTerms.contains(t)) classTerms.prepend(t) }
  }
  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDObjectElementBuilder]

  private def addProperty(property: JsonLDPropertyBuilder) = properties += property.key -> property

  private def addTerm(property: JsonLDPropertyBuilder): Unit = {
    val existing = termIndex.getOrElse(property.term, List.empty)
    termIndex += (property.term -> (property :: existing))
  }

  private def remove(current: JsonLDPropertyBuilder) = {
    removeTerm(current)
    removeProperty(current)
  }

  private def removeProperty(property: JsonLDPropertyBuilder) = {
    properties -= property.key
  }

  private def removeTerm(property: JsonLDPropertyBuilder) = {
    val existing = termIndex.getOrElse(property.term, List.empty)
    val next     = existing.filter(builder => builder.key != property.key)
    if (next.isEmpty) termIndex -= property.term
    else termIndex += (property.term -> next)
  }
}

object JsonLDObjectElementBuilder {

  type Key  = String
  type Term = String
  def empty(key: String, path: JsonPath) =
    new JsonLDObjectElementBuilder(SourceLocation.Unknown, key, SemanticContext.baseIri, path)

  protected def buildObj(
      termIndex: mutable.LinkedHashMap[Term, List[JsonLDPropertyBuilder]],
      classTerms: List[String],
      path: JsonPath,
      ctxBuilder: EntityContextBuilder
  ): JsonLDObject = {
    val fields = termIndex.map {
      case (term, List(builder)) =>
        val (element, elementType) = builder.element.build(ctxBuilder)
        createField(builder, elementType) -> element
      case (term, builders) =>
        val (elements, types) = TupleOps.reduce(builders.map(_.element.build(ctxBuilder)))
        val element           = JsonLDArray(elements)
        val arrayType         = computeType(types)
        Field(
          Type.Array(arrayType),
          ValueType(term),
          ModelDoc(displayName = displayNameForMultipleValuedTerm(builders))
        ) -> element
    }
    createObjectElement(fields, classTerms, path, ctxBuilder)
  }

  private def createObjectElement(
      fields: mutable.LinkedHashMap[Field, JsonLDElement],
      classTerms: List[String],
      path: JsonPath,
      ctxBuilder: EntityContextBuilder
  ) = {
    val entityModel = new JsonLDEntityModel(asValueTerms(classTerms), fields.keys.toList, path)
    ctxBuilder + entityModel
    val dObject = JsonLDObject.empty(entityModel, path.last)
    fields.foreach { case (field, value) => dObject.set(field, value) }
    dObject
  }

  private def asValueTerms(terms: List[Term]) = terms.map(ValueType.apply)

  private def displayNameForMultipleValuedTerm(builders: List[JsonLDPropertyBuilder]) =
    builders.map(_.key).mkString("_")

  private def createField(builder: JsonLDPropertyBuilder, elementType: Type): Field = {
    val finalType = builder.element.getOverriddenType.getOrElse(elementType)
    Field(finalType, ValueType(builder.term), ModelDoc(displayName = builder.key))
  }

}
