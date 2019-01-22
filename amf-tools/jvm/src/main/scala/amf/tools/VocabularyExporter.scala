package amf.tools

import java.io.{File, FileWriter}

import amf.core.metamodel.Type.{Bool, Date, DateTime, Double, EncodedIri, Float, Int, Iri, RegExp, Str, Time}
import amf.core.metamodel.domain._
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.vocabulary.Namespace
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.yaml.model.YDocument
import org.yaml.render.YamlRender

case class VocabularyFile(base: String, usage: String)
case class VocabClassTerm(id: String,
                          displayName: String,
                          description: String,
                          superClasses: Seq[String],
                          properties: Seq[String] = Nil)
case class VocabPropertyTerm(id: String,
                             displayName: String,
                             description: String,
                             superClasses: Seq[String],
                             scalarRange: Option[String],
                             objectRange: Option[String],
                             domain: Set[String])

object VocabularyExporter {

  val conflictive = Seq((Namespace.Document + "RootDomainElement").iri())

  val blacklist: Map[ModelVocabulary, Seq[ModelVocabulary]] = Map(
    ModelVocabularies.AmlDoc -> Seq(ModelVocabularies.Security,
                                    ModelVocabularies.Data,
                                    ModelVocabularies.Http,
                                    ModelVocabularies.Shapes),
    ModelVocabularies.Security -> Seq(ModelVocabularies.Http)
  )

  val reflectionsCoreDoc    = new Reflections("amf.core.metamodel.document", new SubTypesScanner(false))
  val reflectionsCoreDomain = new Reflections("amf.core.metamodel.domain", new SubTypesScanner(false))
  val reflectionsWebApi     = new Reflections("amf.plugins.domain.webapi.metamodel", new SubTypesScanner(false))
  val reflectionsShapes     = new Reflections("amf.plugins.domain.shapes.metamodel", new SubTypesScanner(false))

  var files: Map[String, VocabularyFile]         = Map()
  var classToFile: Map[String, String]           = Map()
  var classes: Map[String, VocabClassTerm]       = Map()
  var properties: Map[String, VocabPropertyTerm] = Map()

  def fillInitialFiles() = {
    ModelVocabularies.all.foreach { vocab: ModelVocabulary =>
      files = files + (vocab.filename -> VocabularyFile(base = vocab.base, usage = vocab.usage))
    }
  }

  def notBlacklisted(klasses: Seq[String], vocabulary: ModelVocabulary) = {
    val vocabBlacklist: Seq[ModelVocabulary] = blacklist.getOrElse(vocabulary, Seq())
    val res = klasses.filter { klassName =>
      val notConflictive = !conflictive.contains(klassName)
      val notInBlackList = !vocabBlacklist.exists { v: ModelVocabulary =>
        //println(s"Checking ${klassName} vs ${v.base} => ${klassName.startsWith(v.base)}")
        klassName.startsWith(v.base)
      }
      notConflictive && notInBlackList
    }

    res
  }

  def renderVocabulary(vocabulary: ModelVocabulary) = {
    val uses         = computeUses(vocabulary, ModelVocabularies.all, _.filename)
    val externals    = computeUses(vocabulary, ExternalModelVocabularies.all, _.base)
    val vocabClasses = classesForVocabulary(vocabulary)
    var vocabProperties = vocabClasses
      .flatMap { klass =>
        klass.properties.filter { p =>
          p.startsWith(vocabulary.base) || ExternalModelVocabularies.all.exists(v => p.startsWith(v.base))
        }
      }
      .map(properties(_))
      .distinct
      .sortBy(_.id)
    vocabProperties = properties.values.foldLeft(vocabProperties) {
      case (acc, prop) =>
        if (prop.id.startsWith(vocabulary.base) && !vocabProperties.contains(prop)) {
          acc :+ prop
        } else {
          acc
        }
    }

    val document = YDocument(b => {
      b.comment("%Vocabulary 1.0")
      b.obj {
        b =>
          b.entry("base", vocabulary.base)
          b.entry("usage", vocabulary.usage)

          // external vocabularies
          if (uses.nonEmpty) {
            b.entry("uses", b => {
              b.obj { b =>
                uses.foreach {
                  case (alias, path) =>
                    b.entry(alias, path)
                }
              }
            })
          }

          // external namespaces
          if (externals.nonEmpty) {
            b.entry("external", b => {
              b.obj { b =>
                externals.foreach {
                  case (alias, path) =>
                    b.entry(alias, path)
                }
              }
            })
          }

          // classTerms
          if (vocabClasses.nonEmpty) {
            b.entry(
              "classTerms",
              b => {
                b.obj {
                  b =>
                    vocabClasses
                      .foreach {
                        classTerm: VocabClassTerm =>
                          b.entry(
                            compactUri(classTerm.id, vocabulary),
                            b => {
                              b.obj {
                                b =>
                                  b.entry("displayName", classTerm.displayName)
                                  if (classTerm.description != "") {
                                    b.entry("description", classTerm.description)
                                  }
                                  val superClasses = notBlacklisted(classTerm.superClasses, vocabulary)
                                  if (superClasses.nonEmpty) {
                                    if (classTerm.superClasses.length == 1) {
                                      b.entry("extends", compactUri(classTerm.superClasses.head, vocabulary))
                                    } else {
                                      b.entry("extends", b => {
                                        b.list { l =>
                                          classTerm.superClasses.foreach { t =>
                                            l += compactUri(t, vocabulary)
                                          }
                                        }
                                      })
                                    }
                                  }

                                  // only properties in domain
                                  /*
                    val exclusiveProperties = classTerm.properties.filter { prop =>
                      properties(prop).domain.size == 1 && properties(prop).domain.head == classTerm.id
                    }
                                   */
                                  val exclusiveProperties = notBlacklisted(classTerm.properties, vocabulary)
                                  if (exclusiveProperties.nonEmpty) {
                                    b.entry("properties", b => {
                                      b.list { l =>
                                        exclusiveProperties.foreach { p =>
                                          l += compactUri(p, vocabulary)
                                        }
                                      }
                                    })
                                  }
                              }
                            }
                          )
                      }
                }
              }
            )
          }

          // property terms
          if (vocabProperties.nonEmpty) {
            b.entry(
              "propertyTerms",
              b => {
                b.obj {
                  b =>
                    vocabProperties
                      .foreach {
                        propertyTerm =>
                          b.entry(
                            compactUri(propertyTerm.id, vocabulary),
                            b => {
                              b.obj {
                                b =>
                                  b.entry("displayName", propertyTerm.displayName)
                                  if (propertyTerm.description != "") {
                                    b.entry("description", propertyTerm.description)
                                  }
                                  val superProperties = notBlacklisted(propertyTerm.superClasses, vocabulary)
                                  if (superProperties.nonEmpty) {
                                    if (propertyTerm.superClasses.length == 1) {
                                      b.entry("extends", compactUri(propertyTerm.superClasses.head, vocabulary))
                                    } else {
                                      b.entry("extends", b => {
                                        b.list { l =>
                                          propertyTerm.superClasses.foreach { t =>
                                            l += compactUri(t, vocabulary)
                                          }
                                        }
                                      })
                                    }
                                  }

                                  if (propertyTerm.scalarRange.nonEmpty) {
                                    b.entry("range", propertyTerm.scalarRange.get)
                                  }
                                  if (propertyTerm.objectRange.nonEmpty) {
                                    if (notBlacklisted(Seq(propertyTerm.objectRange.get), vocabulary).nonEmpty) {
                                      b.entry("range", compactUri(propertyTerm.objectRange.get, vocabulary))
                                    }
                                  }
                              }
                            }
                          )
                      }
                }
              }
            )
          }
      }
    })

    YamlRender.render(document)
  }

  def compactUri(id: String, current: ModelVocabulary): String = {
    (ModelVocabularies.all ++ ExternalModelVocabularies.all).find { vocab =>
      id.startsWith(vocab.base)
    } map { vocab =>
      if (vocab == current) {
        id.replace(vocab.base, "")
      } else {
        vocab.alias + "." + id.replace(vocab.base, "")
      }
    } match {
      case Some(curie) => curie
      case _           => throw new Exception(s"Cannot compact URI ${id}")
    }
  }

  def classesForVocabulary(vocabulary: ModelVocabulary): Seq[VocabClassTerm] = {
    val ids: Seq[String] = classToFile.filter {
      case (k, vocab) =>
        vocab == vocabulary.filename
    } map {
      case (k, _) =>
        classes(k)
    } flatMap { klass =>
      // val superClasses = klass.superClasses.filter(p => !p.startsWith(vocabulary.base))
      // val klassProperties = klass.properties.map(p => properties(p)).map(_.objectRange).collect { case Some(r) => r}
      Seq(klass.id) /*++ superClasses ++ klassProperties */
    } toSeq

    ids.distinct.map(classes(_)).filter { klass: VocabClassTerm =>
      klass.id.startsWith(vocabulary.base) || ExternalModelVocabularies.all.exists(v => klass.id.startsWith(v.base))
    }
  }

  def computeUses(vocabulary: ModelVocabulary,
                  vocabs: Seq[ModelVocabulary],
                  p: ModelVocabulary => String): Map[String, String] = {
    val allIds: Seq[String] = classToFile.filter {
      case (k, vocab) =>
        vocab == vocabulary.filename
    } map {
      case (k, _) =>
        classes(k)
    } flatMap { klass =>
      val superClasses = klass.superClasses.filter(p => !p.startsWith(vocabulary.base))
      val klassProperties =
        klass.properties.map(p => properties(p)).filter(p => !p.id.startsWith(vocabulary.base)).map(_.id)
      val klassRanges = klass.properties.map(p => properties(p)).map(_.objectRange).collect { case Some(r) => r }
      val deps        = superClasses ++ klassProperties ++ klassRanges
      deps
    } toSeq

    val blacklisted = blacklist.getOrElse(vocabulary, Seq())
    allIds.distinct.foldLeft(Map[String, String]()) {
      case (ns, id) =>
        vocabs.find { ns =>
          id.contains(ns.base)
        } match {
          case Some(foundVocabulary) if foundVocabulary != vocabulary && !blacklisted.contains(foundVocabulary) =>
            ns + (foundVocabulary.alias -> p(foundVocabulary))
          case _ => ns
        }
    }
  }

  def metaObjects(reflections: Reflections, handler: String => Any) = {
    reflections.getAllTypes.forEach { className =>
      if (className.endsWith("$")) {
        handler(className)
      }
    }
  }

  def computeRange(fieldType: Type, propertyTerm: VocabPropertyTerm): VocabPropertyTerm = {
    fieldType match {
      case Str | RegExp        => propertyTerm.copy(scalarRange = Some("string"))
      case Int                 => propertyTerm.copy(scalarRange = Some("integer"))
      case Float               => propertyTerm.copy(scalarRange = Some("float"))
      case Double              => propertyTerm.copy(scalarRange = Some("double"))
      case Time                => propertyTerm.copy(scalarRange = Some("time"))
      case Date                => propertyTerm.copy(scalarRange = Some("date"))
      case DateTime            => propertyTerm.copy(scalarRange = Some("dateTime"))
      case Iri                 => propertyTerm.copy(scalarRange = Some("uri"))
      case EncodedIri          => propertyTerm.copy(scalarRange = Some("uri"))
      case Bool                => propertyTerm.copy(scalarRange = Some("boolean"))
      case a: Type.Array       => computeRange(a.element, propertyTerm)
      case a: Type.SortedArray => computeRange(a.element, propertyTerm)
      case other: Obj =>
        val id = other.`type`.head.iri()
        propertyTerm.copy(objectRange = Some(id))
    }
  }

  def buildPropertyTerm(field: Field, klass: VocabClassTerm): Unit = {
    val id = field.value.iri()
    var propertyTerm = properties.get(id) match {
      case Some(prop) => prop
      case None =>
        val doc         = field.doc
        val displayName = doc.displayName
        val description = doc.description
        val vocab       = doc.vocabulary.filename

        var propertyTerm = VocabPropertyTerm(id, displayName, description, doc.superClasses, None, None, Set())

        propertyTerm = computeRange(field.`type`, propertyTerm)
        properties = properties + (propertyTerm.id -> propertyTerm)
        propertyTerm
    }

    // updating references
    propertyTerm.copy(domain = propertyTerm.domain ++ Set(klass.id))
    properties = properties + (propertyTerm.id -> propertyTerm)
    val updatedKlass = klass.copy(properties = klass.properties :+ propertyTerm.id)
    classes = classes + (klass.id -> updatedKlass)
  }

  def buildClassTerm(klassName: String, modelObject: Obj): Option[VocabClassTerm] = {
    val doc         = modelObject.doc
    val types       = modelObject.`type`.map(_.iri())
    val id          = types.head
    val displayName = doc.displayName
    val description = doc.description
    val vocab       = doc.vocabulary.filename

    var superClassesInDoc = types.tail
    val superClassesInInhertiance = Seq(modelObject.getClass.getSuperclass) ++ modelObject.getClass.getInterfaces.toSeq
      .map {
        case klass if klass.getCanonicalName + "$" != klassName =>
          val singletonKlassName = s"${klass.getCanonicalName}$$"
          parseMetaObject(singletonKlassName)
        case _ =>
          None
      } collect { case Some(classTerm: VocabClassTerm) => classTerm } map { classTerm: VocabClassTerm =>
      classTerm.id
    }

    val finalSuperclasses = (superClassesInDoc ++ superClassesInInhertiance).distinct.filter(!conflictive.contains(_))
    var classTerm =
      VocabClassTerm(id = id, displayName = displayName, description = description, superClasses = finalSuperclasses)

    classes = classes + (id         -> classTerm)
    classToFile = classToFile + (id -> vocab)

    // index fields
    modelObject.fields.foreach { field =>
      buildPropertyTerm(field, classTerm)
      classTerm = classes(id) // update after linking in property term
    }

    Some(classTerm)
  }

  def parseMetaObject(klassName: String): Option[VocabClassTerm] = {
    classes.get(klassName) match {
      case cached @ Some(_) => cached
      case _ =>
        try {
          val singleton = Class.forName(klassName)
          singleton.getField("MODULE$").get(singleton) match {
            case modelObject: Obj => buildClassTerm(klassName, modelObject)
            case other            =>
              //println(s"Other thing: $other")
              None
          }
        } catch {
          case _: ClassNotFoundException =>
            //println(s"NOT FOUND '${singletonKlassName}'")
            None
          case _: NoSuchFieldException =>
            //println(s"NOT FIELD '${singletonKlassName}'")
            None
        }
    }
  }

  def main(args: Array[String]): Unit = {

    println("*** Starting")

    // let's initialize the files
    fillInitialFiles()

    println("*** Processing classes")
    metaObjects(reflectionsCoreDoc, parseMetaObject)
    metaObjects(reflectionsCoreDomain, parseMetaObject)
    metaObjects(reflectionsWebApi, parseMetaObject)
    metaObjects(reflectionsShapes, parseMetaObject)

    // review
    println(s"*** Parsed classes: ${classes.keys.toSeq.size}")
    //classes.keys.toSeq.sorted.foreach(k => println(s" - ${k}"))

    println(s"*** Parsed properties: ${properties.keys.toSeq.size}")
    //properties.keys.toSeq.sorted.foreach(k => println(s" - ${k}"))

    Seq(ModelVocabularies.AmlDoc,
        ModelVocabularies.Http,
        ModelVocabularies.Data,
        ModelVocabularies.Shapes,
        ModelVocabularies.Security,
        ModelVocabularies.Meta).foreach { vocab =>
      println(s"**** RENDERING ${vocab.filename}")
      val f      = new File(s"vocabularies/vocabularies/${vocab.filename}")
      val writer = new FileWriter(f)
      try {
        val text = renderVocabulary(vocab)
        //println(text)
        writer.write(text)
      } finally {
        writer.close()
      }
    }
  }

}
