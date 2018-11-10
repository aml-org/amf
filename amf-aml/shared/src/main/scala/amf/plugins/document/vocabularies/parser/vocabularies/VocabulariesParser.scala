package amf.plugins.document.vocabularies.parser.vocabularies

import amf.core.Root
import amf.core.annotations.Aliases
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{BaseSpecParser, ParserContext, _}
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.metamodel.domain.{ClassTermModel, ObjectPropertyTermModel}
import amf.plugins.document.vocabularies.model.document.Vocabulary
import amf.plugins.document.vocabularies.model.domain._
import amf.plugins.document.vocabularies.parser.common.SyntaxErrorReporter
import org.yaml.model._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class VocabularyDeclarations(var externals: Map[String, External] = Map(),
                             var classTerms: Map[String, ClassTerm] = Map(),
                             var propertyTerms: Map[String, PropertyTerm] = Map(),
                             var usedVocabs: Map[String, Vocabulary] = Map(),
                             libs: Map[String, VocabularyDeclarations] = Map(),
                             errorHandler: Option[ErrorHandler],
                             futureDeclarations: FutureDeclarations)
  extends Declarations(libs, Map(), Map(), errorHandler, futureDeclarations) {

  def registerTerm(term: PropertyTerm) = {
    if (!term.name.value().contains(".")) {
      propertyTerms += (term.name.value() -> term)
    }
  }

  def registerTerm(term: ClassTerm) = {
    if (!term.name.value().contains(".")) {
      classTerms += (term.name.value() -> term)
    }
  }

  def registerUsedVocabulary(alias: String, vocab: Vocabulary) = usedVocabs += (alias -> vocab)

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

  def resolveExternal(key: String): Option[String] = {
    if (key.contains(".")) {
      val prefix = key.split("\\.").head
      val value  = key.split("\\.").last
      externals.get(prefix).map(external => s"${external.base.value()}$value")
    } else {
      None
    }
  }

  def resolveExternalNamespace(prefix: Option[String], suffix: String): Try[String] = {
    prefix match {
      case Some(prefixString) =>
        resolveExternal(s"$prefixString.$suffix") match {
          case Some(resolvedPrefix) => Success(resolvedPrefix)
          case _                    => Failure(new Exception(s"Cannot resolve external prefix $prefixString"))
        }
      case _                  => Success((Namespace.Data + suffix).iri())
    }
  }
}


trait VocabularySyntax { this: VocabularyContext =>

  val vocabulary: Map[String,String] = Map(
    "$dialect" -> "string",
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
    map.map.keySet.map(_.as[YScalar].text).foreach { property =>
      allowedProps.get(property) match {
        case Some(_) => // correct
        case None => closedNodeViolation(id, property, nodeType, map)
      }
    }
  }
}

class VocabularyContext(private val wrapped: ParserContext, private val ds: Option[VocabularyDeclarations] = None)
  extends ParserContext(wrapped.rootContextDocument, wrapped.refs, wrapped.futureDeclarations, wrapped.parserCount)
    with VocabularySyntax with SyntaxErrorReporter {

  var imported: Map[String,Vocabulary] = Map()

  def registerVocabulary(alias: String, vocabulary: Vocabulary) = {
    imported += (alias -> vocabulary)
  }


  var pendingLocal: Seq[(String, String, YPart)] = Nil

  def register(alias: String, classTerm: ClassTerm): Unit = {
    pendingLocal = pendingLocal.filter(_._1 != classTerm.id)
    declarations.classTerms += (alias -> classTerm)
  }

  def register(alias: String, propertyTerm: PropertyTerm): Unit = {
    pendingLocal = pendingLocal.filter(_._1 != propertyTerm.id)
    declarations.propertyTerms += (alias -> propertyTerm)
  }


  def resolvePropertyTermAlias(base: String, propertyTermAlias: String, where: YPart, strictLocal: Boolean): Option[String] = {
    if (propertyTermAlias.contains(".")) {
      val prefix = propertyTermAlias.split("\\.").head
      val value = propertyTermAlias.split("\\.").last
      declarations.externals.get(prefix) match {
        case Some(external) => Some(s"${external.base.value()}$value")
        case None => declarations.libraries.get(prefix) match {
          case Some(vocab: VocabularyDeclarations) => vocab.getPropertyTermId(value)
          case _                                   => None
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
        case Some(external) => Some(s"${external.base.value()}$value")
        case None => declarations.libraries.get(prefix) match {
          case Some(vocab: VocabularyDeclarations) => vocab.getClassTermId(value)
          case _                                   => None
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


  def terms(): Seq[DomainElement] = declarations.classTerms.values.toSeq ++ declarations.propertyTerms.values.toSeq
}

case class ReferenceDeclarations(references: mutable.Map[String, Any] = mutable.Map())(implicit ctx: VocabularyContext) {
  def +=(alias: String, unit: BaseUnit): Unit = {
    references += (alias -> unit)
    val library = ctx.declarations.getOrCreateLibrary(alias)
    unit match {
      case d: Vocabulary =>
        ctx.registerVocabulary(alias, d)
        d.declares.foreach {
          case prop: PropertyTerm => library.registerTerm(prop)
          case cls: ClassTerm     => library.registerTerm(cls)
        }
    }
  }

  def += (external: External): Unit = {
    references += (external.alias.value() -> external)
    ctx.declarations.externals += (external.alias.value() -> external)
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
            val alias: String = e.key.as[YScalar].text
            val url: String   = e.value.as[YScalar].text
            target(url).foreach {
              case module: DeclaresModel => result += (alias, collectAlias(module, alias -> (module.id, url)))
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
            val alias: String = e.key.as[YScalar].text
            val base: String   = e.value.as[YScalar].text
            val external = External()
            result += external.withAlias(alias).withBase(base)
          })
    )
  }

  private def collectAlias(module: BaseUnit, alias: (Aliases.Alias, (Aliases.FullUrl, Aliases.RelativeUrl))): BaseUnit = {
    module.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => module.add(Aliases(Set(alias)))
    }
  }
}

class VocabulariesParser(root: Root)(implicit override val ctx: VocabularyContext) extends BaseSpecParser {

  val map: YMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
  val vocabulary: Vocabulary = Vocabulary(Annotations(map)).withLocation(root.location).withId(root.location)

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

    // closed node validation
    ctx.closedNode("vocabulary", vocabulary.id, map)

    val references = VocabulariesReferencesParser(map, root.references).parse(vocabulary.base.value())

    if (ctx.declarations.externals.nonEmpty)
      vocabulary.withExternals(ctx.declarations.externals.values.toSeq)

    parseClassTerms(map)
    parsePropertyTerms(map)


    val declarables = ctx.terms()
    val imported = ctx.imported map { case (alias, library) =>
        VocabularyReference().withAlias(alias).withReference(library.id).withBase(library.base.value()).adopted(vocabulary.id)
    }
    if (imported.nonEmpty)
      vocabulary.withImports(imported.toSeq)
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
    val classTermAlias = classTermDeclaration.key.as[YScalar].text
    classTerm.withName(classTermAlias)

    ctx.resolveClassTermAlias(vocabulary.base.value(), classTermAlias, classTermDeclaration.key, strictLocal = false) match {
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
            case YType.Seq => DefaultArrayNode(entry.value).nodes._1.map(_.value.toString)  // ArrayNode(entry.value).strings().scalars.map(_.toString)
          }

          val properties: Seq[String] = refs.map { term: String =>
            ctx.resolvePropertyTermAlias(vocabulary.base.value(), term, entry.value, strictLocal =  true) match {
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
            case YType.Seq => {
              // ArrayNode(entry.value).strings().scalars.map(_.toString)
              DefaultArrayNode(node = entry.value).nodes._1.map(_.value.toString)
            }
          }

          val superClasses: Seq[String] = refs.map { term: String =>
            ctx.resolveClassTermAlias(vocabulary.base.value(), term, entry.value, strictLocal = true) match {
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
        case Some(value) => value.value.as[YScalar].text match {
          case "string" |  "integer" | "float" | "boolean" | "uri" | "any" | "time" | "date" | "dateTime" =>
            DatatypePropertyTerm(Annotations(propertyTermDeclaration))
          case _ => ObjectPropertyTerm(Annotations(propertyTermDeclaration))
        }
      }
    }

    val propertyTermAlias = propertyTermDeclaration.key.as[YScalar].text
    propertyTerm.withName(propertyTermAlias)

    ctx.resolvePropertyTermAlias(vocabulary.base.value(), propertyTermAlias, propertyTermDeclaration.key, strictLocal = false) match {
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
          val rangeId = entry.value.as[YScalar].text match {
            case "uri" => Some((Namespace.Xsd + "anyUri").iri())
            case "any" => Some((Namespace.Xsd + "anyType").iri())
            case "string" |  "integer" | "float" | "boolean" | "time" | "date" | "dateTime" =>  Some((Namespace.Xsd + entry.value.as[YScalar].text).iri())
            case classAlias =>
              ctx.resolveClassTermAlias(vocabulary.base.value(), classAlias, entry.value, strictLocal = true) match {
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
            case YType.Seq =>
              DefaultArrayNode(entry.value).nodes._1.map(_.as[YScalar].text)
              // ArrayNode(entry.value).strings().scalars.map(_.toString)
          }

          val superClasses: Seq[String] = refs.map { term: String =>
            ctx.resolvePropertyTermAlias(vocabulary.base.value(), term, entry.value, strictLocal = true) match {
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
