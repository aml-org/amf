package amf.plugins.document.vocabularies2.parser.vocabularies

import amf.core.Root
import amf.core.annotations.Aliases
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{BaseSpecParser, ParserContext, _}
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies2.metamodel.domain.{ClassTermModel, ObjectPropertyTermModel}
import amf.plugins.document.vocabularies2.model.document.Vocabulary
import amf.plugins.document.vocabularies2.model.domain._
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YPart, YType}

import scala.collection.mutable

class VocabularyDeclarations(var externals: Map[String, External] = Map(),
                             var classTerms: Map[String, ClassTerm] = Map(),
                             var propertyTerms: Map[String, PropertyTerm] = Map(),
                             libs: Map[String, VocabularyDeclarations] = Map(),
                             errorHandler: Option[ErrorHandler],
                             futureDeclarations: FutureDeclarations)
  extends Declarations(libs, Map(), Map(), errorHandler, futureDeclarations) {

  def registerTerm(term: PropertyTerm) = {
    if (!term.name.contains(".")) {
      propertyTerms += (term.name -> term)
    }
  }

  def registerTerm(term: ClassTerm) = {
    if (!term.name.contains(".")) {
      classTerms += (term.name -> term)
    }
  }

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): VocabularyDeclarations = {
    libraries.get(alias) match {
      case Some(lib: VocabularyDeclarations) => lib
      case _ =>
        val result = new VocabularyDeclarations(errorHandler = errorHandler, futureDeclarations = EmptyFutureDeclarations())
        libraries = libraries + (alias -> result)
        result
    }
  }

  def getTermId(value: String): Option[String] = getPropertyTermId(value).orElse(getClassTermId(value))


  def getPropertyTermId(alias: String): Option[String] = {
    propertyTerms.get(alias) match {
      case Some(pt) => Some(pt.id)
      case None     => None
    }
  }

  def getClassTermId(alias: String): Option[String] = {
    classTerms.get(alias) match {
      case Some(ct) => Some(ct.id)
      case None     => None
    }
  }

}


trait VocabularySyntax { this: VocabularyContext =>

  val vocabulary: Map[String,String] = Map(
    "base" -> "string",
    "usage" -> "string",
    "vocabulary" -> "string",
    "uses" -> "libraries",
    "external" -> "libraries",
    "classTerms" -> "ClassTerm[]",
    "propertyTerms" -> "PropertyTerm[]"
  )

  val classTerm: Map[String,String] = Map(
    "displayName" -> "string",
    "description" -> "string",
    "properties" -> "string[]",
    "extends" -> "string[]"
  )

  val propertyTerm: Map[String,String] = Map(
    "displayName" -> "string",
    "description" -> "string",
    "range" -> "string[]",
    "extends" -> "string[]"
  )

  def closedNode(nodeType: String, id: String, map: YMap): Unit = {
    val allowedProps = nodeType match {
      case "vocabulary" => vocabulary
      case "classTerm" => classTerm
      case "propertyTerm" => propertyTerm
    }
    map.map.keySet.map(_.as[String]).foreach { property =>
      allowedProps.get(property) match {
        case Some(_) => // correct
        case None => closedNodeViolation(id, property, nodeType, map)
      }
    }
  }
}

class VocabularyContext(private val wrapped: ParserContext, private val ds: Option[VocabularyDeclarations] = None)
  extends ParserContext(wrapped.rootContextDocument, wrapped.refs, wrapped.futureDeclarations)
    with VocabularySyntax {

  var pendingLocal: Seq[(String, String, YPart)] = Nil

  def register(alias: String, classTerm: ClassTerm): Unit = {
    pendingLocal = pendingLocal.filter(_._1 != classTerm.id)
    declarations.classTerms += (alias -> classTerm)
  }

  def register(alias: String, propertyTerm: PropertyTerm): Unit = {
    pendingLocal = pendingLocal.filter(_._1 != propertyTerm.id)
    declarations.propertyTerms += (alias -> propertyTerm)
  }


  protected val MissingTermSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-vocabulary-term").iri(),
    "Missing vocabulary term",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )


  def resolvePropertyTermAlias(base: String, propertyTermAlias: String, where: YPart, strictLocal: Boolean): Option[String] = {
    if (propertyTermAlias.contains(".")) {
      val prefix = propertyTermAlias.split("\\.").head
      val value = propertyTermAlias.split("\\.").last
      declarations.externals.get(prefix) match {
        case Some(external) => Some(s"${external.base}$value")
        case None => declarations.libraries.get(prefix) match {
          case Some(vocab: VocabularyDeclarations) => vocab.getPropertyTermId(value)
          case None => None
        }
      }
    } else {
      val local = s"$base$propertyTermAlias"
      declarations.getPropertyTermId(propertyTermAlias) match {
        case Some(_) => // ignore
        case None    => if (strictLocal) { pendingLocal ++= Seq((local, propertyTermAlias, where)) }
      }
      Some(local)
    }
  }

  def resolveClassTermAlias(base: String, classTermAlias: String, where: YPart, strictLocal: Boolean): Option[String] = {
    if (classTermAlias.contains(".")) {
      val prefix = classTermAlias.split("\\.").head
      val value = classTermAlias.split("\\.").last
      declarations.externals.get(prefix) match {
        case Some(external) => Some(s"${external.base}$value")
        case None => declarations.libraries.get(prefix) match {
          case Some(vocab: VocabularyDeclarations) => vocab.getClassTermId(value)
          case None => None
        }
      }
    } else {
      val local = s"$base$classTermAlias"
      declarations.getClassTermId(classTermAlias) match {
        case Some(_) => // ignore
        case None    => if (strictLocal) { pendingLocal ++= Seq((local, classTermAlias, where)) }
      }
      Some(local)
    }
  }

  val declarations: VocabularyDeclarations =
    ds.getOrElse(new VocabularyDeclarations(errorHandler = Some(this), futureDeclarations = futureDeclarations))

  def missingTermViolation(term: String, node: String, ast: YPart) = {
    violation(MissingTermSpecification.id(), node, s"Cannot find vocabulary term $term", ast)
  }

  def closedNodeViolation(id: String, property: String, nodeType: String, ast: YPart) = {
    violation(
      ParserSideValidations.ClosedShapeSpecification.id(),
      id,
      s"Property: '$property' not supported in a $nodeType node",
      ast
    )
  }
  def terms(): Seq[DomainElement] = declarations.classTerms.values.toSeq ++ declarations.propertyTerms.values.toSeq
}

case class ReferenceDeclarations(references: mutable.Map[String, Any] = mutable.Map())(implicit ctx: VocabularyContext) {
  def +=(alias: String, unit: BaseUnit): Unit = {
    references += (alias -> unit)
    val library = ctx.declarations.getOrCreateLibrary(alias)
    unit match {
      case d: Vocabulary =>
        d.declares.foreach {
          case prop: PropertyTerm => library.registerTerm(prop)
          case cls: ClassTerm     => library.registerTerm(cls)
        }
    }
  }

  def += (external: External): Unit = {
    references += (external.alias -> external)
    ctx.declarations.externals += (external.alias -> external)
  }

  def baseUnitReferences(): Seq[BaseUnit] = references.values.toSet.filter(_.isInstanceOf[BaseUnit]).toSeq.asInstanceOf[Seq[BaseUnit]]
}

case class VocabulariesReferencesParser(map: YMap, references: Seq[ParsedReference])(implicit ctx: VocabularyContext) {

  def parse(location: String): ReferenceDeclarations = {
    val result = ReferenceDeclarations()
    parseLibraries(result, location)
    parseExternals(result, location)
    result
  }

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)


  private def parseLibraries(result: ReferenceDeclarations, id: String): Unit = {
    map.key(
      "uses",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key
            val url: String   = e.value
            target(url).foreach {
              case module: DeclaresModel => result += (alias, collectAlias(module, alias -> url))
              case other =>
                ctx.violation(id, s"Expected vocabulary module but found: $other", e) // todo Uses should only reference modules...
            }
          })
    )
  }

  private def parseExternals(result: ReferenceDeclarations, id: String): Unit = {
    map.key(
      "external",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key
            val base: String   = e.value
            val external = External()
            result += external.withAlias(alias).withBase(base)
          })
    )
  }

  private def collectAlias(module: BaseUnit, alias: (String, String)): BaseUnit = {
    module.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => module.add(Aliases(Set(alias)))
    }
  }
}

class RamlVocabulariesParser(root: Root)(implicit override val ctx: VocabularyContext) extends BaseSpecParser {

  val map = root.parsed.document.as[YMap]
  val vocabulary = Vocabulary(Annotations(map))

  def parseDocument(): BaseUnit = {

    map.key("base", entry => {
      val value = ValueNode(entry.value)
      vocabulary.set(VocabularyModel.Base, value.string(), Annotations(entry))
    })

    map.key("vocabulary", entry => {
      val value = ValueNode(entry.value)
      vocabulary.set(VocabularyModel.Name, value.string(), Annotations(entry))
    })

    map.key("usage", entry => {
      val value = ValueNode(entry.value)
      vocabulary.set(VocabularyModel.Usage, value.string(), Annotations(entry))
    })

    // base must have a value and is different from the location, but ID is the base
    vocabulary.withLocation(root.location)
    vocabulary.adopted(vocabulary.base)

    // closed node validation
    ctx.closedNode("vocabulary", vocabulary.id, map)

    val references = VocabulariesReferencesParser(map, root.references).parse(vocabulary.base)

    if (ctx.declarations.externals.nonEmpty)
      vocabulary.withExternals(ctx.declarations.externals.values.toSeq)

    parseClassTerms(map)
    parsePropertyTerms(map)


    val declarables = ctx.terms()
    if (declarables.nonEmpty) vocabulary.withDeclares(declarables)
    if (references.references.nonEmpty) vocabulary.withReferences(references.baseUnitReferences())
    // we raise exceptions for missing terms
    ctx.pendingLocal.foreach { case (term, alias, location) =>
      ctx.missingTermViolation(term, vocabulary.id, location)
    }

    vocabulary
  }


  def parseClassTerms(map: YMap) = {
    map.key("classTerms" , entry => {
      val classDeclarations = entry.value.as[YMap]
      classDeclarations.entries.foreach { classTermDeclaration =>
        parseClassTerm(classTermDeclaration)
      }
    })
  }

  def parseClassTerm(classTermDeclaration: YMapEntry): Unit = {
    val classTerm = ClassTerm(Annotations(classTermDeclaration))
    val classTermAlias = classTermDeclaration.key.as[String]
    classTerm.withName(classTermAlias)

    ctx.resolveClassTermAlias(vocabulary.id, classTermAlias, classTermDeclaration.key, strictLocal = false) match {
      case None     => ctx.missingTermViolation(classTermAlias, vocabulary.id, classTermDeclaration.key)
      case Some(id) => classTerm.id = id
    }

    classTermDeclaration.value.tagType match{
      case  YType.Null   => // just declaration
      case _ =>
        val classTermMap = classTermDeclaration.value.as[YMap]
        ctx.closedNode("classTerm", classTerm.id, classTermMap)

        classTermMap.key("displayName", entry => {
          val value = ValueNode(entry.value)
          classTerm.set(ClassTermModel.DisplayName, value.string())
        })

        classTermMap.key("description", entry => {
          val value = ValueNode(entry.value)
          classTerm.set(ClassTermModel.Description, value.string())
        })

        classTermMap.key("properties", entry => {
          val refs: Seq[String] = entry.value.tagType match {
            case YType.Str => Seq(ValueNode(entry.value).string().toString)
            case YType.Seq => ArrayNode(entry.value).strings().scalars.map(_.toString)
          }

          val properties: Seq[String] = refs.map { term: String =>
            ctx.resolvePropertyTermAlias(vocabulary.id, term, entry.value, strictLocal =  true) match {
              case Some(v) => Some(v)
              case None =>
                ctx.missingTermViolation(term, vocabulary.id, entry.value)
                None
            }
          }.filter(_.nonEmpty).map(_.get)

          if (properties.nonEmpty)
            classTerm.set(ClassTermModel.Properties, properties)
        })

        classTermMap.key("extends", entry => {
          val refs: Seq[String] = entry.value.tagType match {
            case YType.Str => Seq(ValueNode(entry.value).string().toString)
            case YType.Seq => ArrayNode(entry.value).strings().scalars.map(_.toString)
          }

          val superClasses: Seq[String] = refs.map { term: String =>
            ctx.resolveClassTermAlias(vocabulary.id, term, entry.value, strictLocal = true) match {
              case Some(v) => Some(v)
              case None =>
                ctx.missingTermViolation(term, vocabulary.id, entry.value)
                None
            }
          }.filter(_.nonEmpty).map(_.get)

          classTerm.set(ClassTermModel.SubClassOf, superClasses)
        })
    }

    ctx.register(classTermAlias, classTerm)
  }

  def parsePropertyTerms(map: YMap) = {
    map.key("propertyTerms" , entry => {
      val classDeclarations = entry.value.as[YMap]
      classDeclarations.entries.foreach { propertyTermDeclaration =>
        parsePropertyTerm(propertyTermDeclaration)
      }
    })
  }

  def parsePropertyTerm(propertyTermDeclaration: YMapEntry): Unit = {
    val propertyTerm: PropertyTerm = propertyTermDeclaration.value.tagType match {
      case YType.Null => DatatypePropertyTerm(Annotations(propertyTermDeclaration))
      case _          => propertyTermDeclaration.value.as[YMap].key("range") match {
        case None        => DatatypePropertyTerm(Annotations(propertyTermDeclaration))
        case Some(value) => value.value.as[String] match {
          case "string" |  "integer" | "float" | "boolean" | "uri" | "any" => DatatypePropertyTerm(Annotations(propertyTermDeclaration))
          case _                                                           => ObjectPropertyTerm(Annotations(propertyTermDeclaration))
        }
      }
    }

    val propertyTermAlias = propertyTermDeclaration.key.as[String]
    propertyTerm.withName(propertyTermAlias)

    ctx.resolvePropertyTermAlias(vocabulary.id, propertyTermAlias, propertyTermDeclaration.key, strictLocal = false) match {
      case None     => ctx.missingTermViolation(propertyTermAlias, vocabulary.id, propertyTermDeclaration.key)
      case Some(id) => propertyTerm.id = id
    }

    propertyTermDeclaration.value.tagType match {
      case YType.Null => // ignore
      case _ =>
        val propertyTermMap = propertyTermDeclaration.value.as[YMap]
        ctx.closedNode("propertyTerm", propertyTerm.id, propertyTermMap)

        propertyTermMap.key("displayName", entry => {
          val value = ValueNode(entry.value)
          propertyTerm.set(ClassTermModel.DisplayName, value.string())
        })

        propertyTermMap.key("description", entry => {
          val value = ValueNode(entry.value)
          propertyTerm.set(ClassTermModel.Description, value.string())
        })

        propertyTermMap.key("range", entry => {
          val rangeId = entry.value.as[String] match {
            case "uri" => Some((Namespace.Xsd + "anyUri").iri())
            case "any" => Some((Namespace.Xsd + "anyType").iri())
            case "string" |  "integer" | "float" | "boolean" =>  Some((Namespace.Xsd + entry.value.as[String]).iri())
            case classAlias =>
              ctx.resolveClassTermAlias(vocabulary.id, classAlias, entry.value, strictLocal = true) match {
                case Some(classTermId) => Some(classTermId)
                case None              =>
                  ctx.missingTermViolation(classAlias, vocabulary.id, entry.value)
                  None
              }
          }

          rangeId match {
            case Some(id: String) => propertyTerm.withRange(id)
            case None     => // ignore
          }
        })

        propertyTermMap.key("extends", entry => {
          val refs: Seq[String] = entry.value.tagType match {
            case YType.Str => Seq(ValueNode(entry.value).string().toString)
            case YType.Seq => ArrayNode(entry.value).strings().scalars.map(_.toString)
          }

          val superClasses: Seq[String] = refs.map { term: String =>
            ctx.resolvePropertyTermAlias(vocabulary.id, term, entry.value, strictLocal = true) match {
              case Some(v) => Some(v)
              case None =>
                ctx.missingTermViolation(term, vocabulary.id, entry.value)
                None
            }
          }.filter(_.nonEmpty).map(_.get)

          propertyTerm.set(ObjectPropertyTermModel.SubPropertyOf, superClasses)
        })
    }

    ctx.register(propertyTermAlias, propertyTerm)
  }
}
