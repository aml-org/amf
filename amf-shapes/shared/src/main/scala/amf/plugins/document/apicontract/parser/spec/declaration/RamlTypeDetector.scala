package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain._
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.RamlTypeDefMatcher.{
  JSONSchema,
  XMLSchema,
  isWellKnownType,
  matchWellKnownType
}
import amf.plugins.document.apicontract.parser.spec.declaration.RamlTypeDetection.parseFormat
import amf.plugins.document.apicontract.parser.spec.raml.expression.RamlExpressionParser
import amf.plugins.document.apicontract.parser._
import amf.plugins.domain.shapes.models.TypeDef.{JSONSchemaType, _}
import amf.plugins.domain.shapes.models.{ScalarShape, _}
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping._
import amf.validations.ShapeParserSideValidations._
import amf.validations.ShapeResolutionSideValidations.InvalidTypeInheritanceErrorSpecification
import org.yaml.model._

object RamlTypeDetection {
  def apply(node: YNode, parent: String, format: Option[String] = None, defaultType: DefaultType = StringDefaultType)(
      implicit ctx: ShapeParserContext): Option[TypeDef] =
    RamlTypeDetector(parent, format, defaultType).detect(node)

  def parseFormat(node: YNode): Option[String] =
    node
      .toOption[YMap]
      .flatMap { m =>
        m.key("format").orElse(m.key("format".asRamlAnnotation)).map(_.value.toString())
      }
}

case class RamlTypeDetector(parent: String,
                            format: Option[String] = None,
                            defaultType: DefaultType = StringDefaultType,
                            recursive: Boolean = false,
                            isExplicit: Boolean = false)(implicit ctx: ShapeParserContext)
    extends RamlTypeSyntax {

  def detect(node: YNode): Option[TypeDef] = node.tagType match {

    case YType.Seq =>
      val sequence = node.as[Seq[YNode]]
      InheritsTypeDetecter(collectTypeDefs(sequence), node).orElse(Some(ObjectType)) // type expression type?

    case YType.Map => detectOrInferType(node)

    case YType.Null => Some(defaultType.typeDef)

    case _ =>
      val scalar = node.as[YScalar]
      scalar.text match {
        case t if isRamlVariable(t) && ctx.ramlContextType == RamlWebApiContextType.DEFAULT =>
          throwInvalidAbstractDeclarationError(node, t)
          None
        case t if isRamlVariable(t) && ctx.ramlContextType != RamlWebApiContextType.DEFAULT => None
//        case XMLSchema(_) | JSONSchema(_) if isExplicit => Some(ExternalSchemaWrapper)
        case XMLSchema(_)                               => Some(XMLSchemaType)
        case JSONSchema(_)                              => Some(JSONSchemaType)
        case RamlTypeDefMatcher.TypeExpression(text)    => parseAndMatchTypeExpression(node, text)
        case t if t.endsWith("?")                       => Some(NilUnionType)
        case t: String if !isWellKnownType(TypeName(t)) =>
          // it might be a named type
          // its for identify the type, so i can search in all the scope, no need to difference between named ref and includes.

          ctx.findType(scalar.text, SearchScope.All) match {
            case Some(ancestor) if recursive => ShapeClassTypeDefMatcher(ancestor, node, recursive)
            case Some(_) if !recursive       => Some(ObjectType)
            case None                        => Some(UndefinedType)
          }
        case _ =>
          val text = scalar.text
          format
            .map(f => matchWellKnownType(TypeName(text, f)))
            .orElse(Some(matchWellKnownType(TypeName(text))))
      }
  }

  private def detectOrInferType(node: YNode) = {
    val map          = node.as[YMap]
    val typeExplicit = detectExplicitTypeOrSchema(map)
    val inferred     = inferTypeFrom(map)
    (typeExplicit, inferred) match {
      case (Some(JSONSchemaType), Some(_)) =>
        ctx.eh.warning(
          JsonSchemaInheritanceWarning,
          parent,
          Some(ShapeModel.Inherits.value.iri()),
          "Inheritance from JSON Schema",
          node.value
        )
        typeExplicit
      case (Some(_), _)    => typeExplicit
      case (None, Some(_)) => inferred
      case _               => inferTypeFromPossibleShapeFacets(map)
    }
  }

  private def parseAndMatchTypeExpression(node: YNode, text: String) = {
    RamlExpressionParser
      .check(shape => shape.withId("/"), text)
      .flatMap(s => ShapeClassTypeDefMatcher(s, node, recursive))
      .map {
        case TypeDef.UnionType | TypeDef.ArrayType if !recursive => TypeExpressionType
        case other                                               => other
      }
    // exception case when F: C|D (not type, not recursion, union but only have a typeexpression to parse de union
  }

  private def throwInvalidAbstractDeclarationError(node: YNode, t: String) = {
    ctx.eh.violation(InvalidAbstractDeclarationParameterInType,
                     parent,
                     s"Resource Type/Trait parameter $t in type",
                     node)
  }

  private def isRamlVariable(t: String) = t.startsWith("<<") && t.endsWith(">>")

  private def inferTypeFrom(map: YMap) = {
    val entries   = map.entries.filter(e => !isPatternProperty(e))
    val filterMap = YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))
    val infer = detectItems(filterMap)
      .orElse(detectFileTypes(filterMap))
      .orElse(detectProperties(filterMap))
      .orElse(detectAnyOf(filterMap))
    infer
  }

  private def isPatternProperty(e: YMapEntry) = e.key.as[YScalar].text.matches(".*/.*")

  private def detectProperties(map: YMap): Option[TypeDef] = map.key("properties").map(_ => ObjectType)

  private def detectFileTypes(map: YMap): Option[TypeDef] = map.key("fileTypes").map(_ => FileType)

  private def detectItems(map: YMap): Option[TypeDef] = map.key("items").map(_ => ArrayType)

  private def detectAnyOf(map: YMap): Option[TypeDef] = map.key("anyOf").map(_ => UnionType)

  private def detectExplicitTypeOrSchema(map: YMap): Option[TypeDef] = {
    typeOrSchema(map).flatMap { e =>
      val result = RamlTypeDetector(parent, parseFormat(map), recursive = true, isExplicit = true).detect(e.value)
      result match {
        case Some(t) if t == UndefinedType || isExactlyAny(e, t) => None
        case Some(ExternalSchemaWrapper)                         => None
        case Some(other)                                         => Some(other)
        case None                                                => result
      }
    }
  }

  private def isExactlyAny(e: YMapEntry, t: TypeDef) = t == AnyType && e.value.toString() != "any"

  private def inferTypeFromPossibleShapeFacets(map: YMap) =
    ShapeClassTypeDefMatcher.fetchByRamlSyntax(map).orElse(Some(defaultType.typeDef))

  /** Get type or schema facet. If both are available, default to type facet and throw a validation error. */
  def typeOrSchema(map: YMap): Option[YMapEntry] = {
    val `type` = map.key("type")
    val schema = map.key("schema")

    for {
      _ <- `type`
      s <- schema
    } {
      ctx.eh.violation(ExclusiveSchemaType, parent, "'schema' and 'type' properties are mutually exclusive", s.key)
    }

    schema.foreach(
      s =>
        ctx.eh.warning(SchemaDeprecated,
                       parent,
                       "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead",
                       s.key))

    `type`.orElse(schema)
  }

  private def collectTypeDefs(sequence: Seq[YNode]): List[TypeDef] =
    sequence
      .flatMap(node => RamlTypeDetector(parent, recursive = true).detect(node))
      .toList

  object InheritsTypeDetecter {
    def apply(inheritsTypes: List[TypeDef], ast: YPart): Option[TypeDef] = {
      inheritsTypes match {
        case Nil         => None
        case head :: Nil => Some(head)
        case _ =>
          val definedTypes = inheritsTypes.filter(_ != UndefinedType)
          if (definedTypes.isEmpty) Some(UndefinedType)
          else {
            val head = definedTypes.headOption
            if (inheritsHasDifferentSuperClasses(definedTypes)) {
              ctx.eh.violation(InvalidTypeInheritanceErrorSpecification,
                               parent,
                               "Can't inherit from more than one class type",
                               ast)
              Some(UndefinedType)
            } else head
          }
      }
    }

    def shapeToType(inherits: Seq[Shape], part: YNode)(implicit ctx: ShapeParserContext): Option[TypeDef] =
      apply(inherits.flatMap(s => ShapeClassTypeDefMatcher(s, part, plainUnion = true)).toList, part)
  }

  private def inheritsHasDifferentSuperClasses(definedTypes: List[TypeDef]) = {
    val head = definedTypes.headOption
    definedTypes.count(_.equals(head.get)) != definedTypes.size
  }

  object ShapeClassTypeDefMatcher {
    def apply(shape: Shape, part: YNode, plainUnion: Boolean)(implicit ctx: ShapeParserContext): Option[TypeDef] =
      shape match {
        case _ if shape.isLink =>
          shape.linkTarget match {
            case Some(linkedShape: Shape) if linkedShape == shape => Some(AnyType)
            case Some(linkedShape: Shape)                         => apply(linkedShape, part, plainUnion)
            case _ =>
              ctx.eh.violation(InvalidTypeDefinition,
                               shape.id,
                               "Found reference to domain element different of Shape when shape was expected",
                               part)
              None
          }
        case _: NilShape => Some(NilType)
        case s: ScalarShape =>
          val typeName = RamlTypeDefStringValueMatcher.matchType(typeDef(s.dataType.value()), s.format.option())
          Some(matchWellKnownType(typeName))
        case union: UnionShape => if (plainUnion) simplifyUnionComponentsToType(union, part) else Some(UnionType)
        case _: NodeShape      => Some(ObjectType)
        case _: ArrayShape     => Some(ArrayType)
        case _: MatrixShape    => Some(ArrayType)
        case _: AnyShape       => Some(AnyType)
        case _                 => None
      }

    private def simplifyUnionComponentsToType(union: UnionShape, part: YPart)(
        implicit ctx: ShapeParserContext): Option[TypeDef] = {
      val typeSet =
        union.anyOf.flatMap(t => ShapeClassTypeDefMatcher(t, part.asInstanceOf[YNode], plainUnion = true)).toSet
      if (typeSet.size == 1) Some(typeSet.head)
      else Some(UnionType)
    }

    private def findShapesThatSupportKeysFrom(map: YMap): Seq[String] = {
      val shapesNodes = ctx.syntax.nodes.filterKeys(k => k.endsWith("Shape") && k != "schemaShape")

      var possibles: Seq[String] = Seq()
      map.entries.foreach { entry =>
        val locals = shapesNodes.filter(value => value._2(entry.key.toString()))
        if (locals.nonEmpty) {
          if (possibles.isEmpty) possibles = locals.keys.toSeq
          else possibles = locals.keys.filter(k => possibles.contains(k)).toSeq
        }
      }

      possibles.distinct
    }

    def fetchByRamlSyntax(map: YMap): Option[TypeDef] = {
      val defs = findShapesThatSupportKeysFrom(map).toList
      Option(defs.filter(!_.equals("shape")) match {
        case Nil => defaultType.typeDef
        case head :: Nil =>
          head match {
            case "nodeShape"         => ObjectType
            case "arrayShape"        => ArrayType
            case "stringScalarShape" => TypeDef.StrType
            case "numberScalarShape" => TypeDef.IntType
            case "fileShape"         => TypeDef.FileType
          }
        // explicit inheritance
        case _ :: tail if tail.nonEmpty && map.key("type").isDefined || map.key("schema").isDefined => TypeDef.AnyType
        // multiple matches without inheritance
        case _ :: tail if tail.nonEmpty => MultipleMatch
      })
    }

  }

}
