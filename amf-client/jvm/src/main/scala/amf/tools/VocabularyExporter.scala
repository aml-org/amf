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

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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

  val externalDescription: Map[String, String] = Map(
    (Namespace.Owl + "Class").iri() ->
      "An owl:Class defines a group of individuals that belong together because they share some properties.",
    (Namespace.Owl + "DatatypeProperty").iri() ->
      "Data properties connect individuals with literals. In some knowledge representation systems, functional data properties are called attributes.",
    (Namespace.Owl + "ObjectProperty").iri() ->
      "Object properties connect pairs of individuals.",
    (Namespace.Owl + "Ontology").iri() ->
      "Set of axioms — statements that say what is true in the domain.",
    (Namespace.Rdf + "Property").iri() ->
      "rdf:Property is the class of RDF properties. rdf:Property is an instance of rdfs:Class.",
    (Namespace.Rdfs + "member").iri() ->
      "rdfs:member is an instance of rdf:Property that is a super-property of all the container membership properties i.e. each container membership property has an rdfs:subPropertyOf relationship to the property rdfs:member.",
    (Namespace.Rdfs + "range").iri() ->
      "rdfs:range is an instance of rdf:Property that is used to state that the values of a property are instances of one or more classes.",
    (Namespace.Rdfs + "subClassOf").iri() ->
      "The property rdfs:subClassOf is an instance of rdf:Property that is used to state that all the instances of one class are instances of another.",
    (Namespace.Rdfs + "subPropertyOf").iri() ->
      "The property rdfs:subPropertyOf is an instance of rdf:Property that is used to state that all resources related by one property are also related by another.",
    (Namespace.Shacl + "Shape").iri() ->
      "shacl:Shape is the SHACL superclass of those two shape types in the SHACL vocabulary. Its subclasses shacl:NodeShape and shacl:PropertyShape can be used as SHACL type of node and property shapes, respectively."
  )

  val dependencies: mutable.Map[String, ArrayBuffer[ModelVocabulary]] =
    mutable.Map[String, mutable.ArrayBuffer[ModelVocabulary]]()

  val externalVocabularyClasses: mutable.Map[String, ArrayBuffer[VocabClassTerm]] =
    ExternalModelVocabularies.all.foldLeft(mutable.Map[String, mutable.ArrayBuffer[VocabClassTerm]]()) {
      case (acc, externalVocab) =>
        acc + (externalVocab.alias -> mutable.ArrayBuffer[VocabClassTerm]())
    }
  val externalVocabularyProperties: mutable.Map[String, ArrayBuffer[VocabPropertyTerm]] =
    ExternalModelVocabularies.all.foldLeft(mutable.Map[String, mutable.ArrayBuffer[VocabPropertyTerm]]()) {
      case (acc, externalVocab) =>
        acc + (externalVocab.alias -> mutable.ArrayBuffer[VocabPropertyTerm]())
    }

  def isExternal(prefix: String): Boolean = ExternalModelVocabularies.all.exists(_.alias == prefix)

  def allClasses: Seq[VocabClassTerm] = classToFile.keys.map(classes).toSeq

  def allProperties: Seq[VocabPropertyTerm] =
    allClasses
      .flatMap { klass =>
        klass.properties
      }
      .map { propertyId =>
        properties(propertyId)
      } ++
      Seq(
        fieldToVocabProperty(CanonicalWebAPISpecDialectExporter.DesignLinkTargetField),
        fieldToVocabProperty(CanonicalWebAPISpecDialectExporter.DesignAnnotationField),
        fieldToVocabProperty(CanonicalWebAPISpecDialectExporter.DataPropertiesField)
      )

  def findNamespace(id: String): Option[ModelVocabulary] = {
    (ModelVocabularies.all ++ ExternalModelVocabularies.all).find { vocab =>
      id.startsWith(vocab.base)
    }
  }

  val conflictive: Seq[String] = Seq(
    (Namespace.Document + "RootDomainElement").iri(),
    (Namespace.Document + "DomainElement").iri(),
    (Namespace.Document + "Linkable").iri(),
    (Namespace.ApiContract + "DomainExtension").iri()
  )

  val blacklist: Map[ModelVocabulary, Seq[ModelVocabulary]] = Map()

  val reflectionsExtensions = new Reflections("amf.core.metamodel.domain.extensions", new SubTypesScanner(false))
  val reflectionsCoreDoc    = new Reflections("amf.core.metamodel.document", new SubTypesScanner(false))
  val reflectionsCoreDomain = new Reflections("amf.core.metamodel.domain", new SubTypesScanner(false))
  val reflectionsWebApi     = new Reflections("amf.plugins.domain.webapi.metamodel", new SubTypesScanner(false))
  val reflectionsWebApiDoc  = new Reflections("amf.plugins.document.webapi.metamodel", new SubTypesScanner(false))
  val reflectionsTemplates =
    new Reflections("amf.plugins.domain.webapi.metamodel.templates", new SubTypesScanner(false))
  val reflectionsShapes = new Reflections("amf.plugins.domain.shapes.metamodel", new SubTypesScanner(false))
  val reflectionsVocabularies =
    new Reflections("amf.plugins.document.vocabularies.metamodel.domain", new SubTypesScanner(false))
  val reflectionsVocabDoc =
    new Reflections("amf.plugins.document.vocabularies.metamodel.document", new SubTypesScanner(false))
  val reflectionsExtModel = new Reflections("amf.tools", new SubTypesScanner(false))

  var files: Map[String, VocabularyFile]         = Map()
  var classToFile: Map[String, String]           = Map()
  var classes: Map[String, VocabClassTerm]       = Map()
  var properties: Map[String, VocabPropertyTerm] = Map()

  def fillInitialFiles(): Unit = {
    ModelVocabularies.all.foreach { vocab: ModelVocabulary =>
      files = files + (vocab.filename -> VocabularyFile(base = vocab.base, usage = vocab.usage))
    }
  }

  val emitProperties = false
  def notBlacklisted(klasses: Seq[String], vocabulary: ModelVocabulary): Seq[String] = {
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

  val ShaclShape: String  = (Namespace.Shacl + "Shape").iri()
  val ShapesShape: String = (Namespace.Shapes + "Shape").iri()
  val AnyShape: String    = (Namespace.Shapes + "AnyShape").iri()
  def blacklistedSuperClass(klass: String, superClass: String): Boolean = {
    (klass, superClass) match {
      case (ShapesShape, ShaclShape)                                            => false
      case (AnyShape, ShapesShape)                                              => false
      case (AnyShape, ShaclShape)                                               => true
      case (base, ShaclShape) if base.startsWith(ModelVocabularies.Shapes.base) => true
      case (_, ShapesShape)                                                     => true
      case _                                                                    => false
    }
  }

  def renderVocabulary(vocabulary: ModelVocabulary): String = {
    val vocabClasses    = classesForVocabulary(vocabulary)
    val vocabProperties = propertiesForVocabulary(vocabulary)
    val uses            = usesReferences(vocabulary, vocabClasses, vocabProperties)

    val document = YDocument(b => {
      b.comment("%Vocabulary 1.0")
      b.obj {
        b =>
          b.entry("base", vocabulary.base)
          b.entry("usage", vocabulary.usage)

          // vocabularies
          if (uses.nonEmpty) {
            if (ExternalModelVocabularies.all.contains(vocabulary)) {
              b.entry("external", b => {
                b.obj { b =>
                  uses.foreach(vocab => b.entry(vocab.alias, vocab.base))
                }
              })
            } else {
              b.entry(
                "uses",
                b => {
                  b.obj { b =>
                    uses.foreach(vocab =>
                      if (ExternalModelVocabularies.all.contains(vocab)) {
                        b.entry(vocab.alias, s"external/${vocab.filename}")
                      } else {
                        b.entry(vocab.alias, vocab.filename)
                    })
                  }
                }
              )
            }
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
                                  if (classTerm.displayName != null && classTerm.displayName != "") {
                                    b.entry("displayName", classTerm.displayName)
                                  } else {
                                    b.entry("displayName", uriAlias(classTerm.id))
                                  }
                                  if (classTerm.description != "") {
                                    b.entry("description", classTerm.description)
                                  } else if (externalDescription.get(classTerm.id).isDefined) {
                                    b.entry("description", externalDescription(classTerm.id))
                                  }
                                  val superClasses = notBlacklisted(classTerm.superClasses, vocabulary).filter(sk =>
                                    !blacklistedSuperClass(classTerm.id, sk))
                                  if (superClasses.nonEmpty) {
                                    if (superClasses.length == 1) {
                                      b.entry("extends", compactUri(superClasses.head, vocabulary))
                                    } else {
                                      b.entry("extends", b => {
                                        b.list { l =>
                                          superClasses.foreach { t =>
                                            l += compactUri(t, vocabulary)
                                          }
                                        }
                                      })
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
                                  if (propertyTerm.displayName != null && propertyTerm.displayName != "") {
                                    b.entry("displayName", propertyTerm.displayName)
                                  } else {
                                    b.entry("displayName", uriAlias(propertyTerm.id))
                                  }
                                  if (propertyTerm.description != "") {
                                    b.entry("description", propertyTerm.description)
                                  } else if (externalDescription.get(propertyTerm.id).isDefined) {
                                    b.entry("description", externalDescription(propertyTerm.id))
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

  def uriAlias(uri: String): String = {
    if (uri.contains("#")) {
      uri.split("#").last
    } else {
      uri.split("/").last
    }
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
      case _           => throw new Exception(s"Cannot compact URI $id")
    }
  }

  def propertiesForVocabulary(vocabulary: ModelVocabulary): Seq[VocabPropertyTerm] = {
    // Properties declared as belonging to this vocabulary
    val explicitProperties: Seq[VocabPropertyTerm] =
      allProperties.filter(property => property.id.startsWith(vocabulary.base)).distinct
    val explicitPropertiesMap = explicitProperties.foldLeft(Map[String, VocabPropertyTerm]()) {
      case (acc, property) =>
        acc + (property.id -> property)
    }

    // Properties in this vocabulary because they are used in extension from other properties
    val implicitPropertiesExtension: Seq[(String, VocabPropertyTerm)] = allProperties.flatMap { property =>
      property.superClasses.filter { superClass =>
        superClass.startsWith(vocabulary.base) && !explicitPropertiesMap.contains(superClass)
      } map { superProperty =>
        (superProperty, property)
      }
    }

    // Implicit IDs -> simple property terms
    val implicitProperties = implicitPropertiesExtension.distinct.map {
      case (propertyId: String, property: VocabPropertyTerm) =>
        VocabPropertyTerm(
          id = propertyId,
          displayName = uriAlias(propertyId),
          description = "",
          superClasses = Seq(),
          scalarRange = property.scalarRange,
          objectRange = property.objectRange,
          domain = Set()
        )
    }

    // final set of properties
    (explicitProperties ++ implicitProperties).sortBy(_.id)
  }

  /**
    * Load all classes for a given vocabulary.
    * Classes are returned because they are declared explicitly or used in extends or ranges.
    */
  def classesForVocabulary(vocabulary: ModelVocabulary): Seq[VocabClassTerm] = {
    // Classes declared as belonging to this vocabulary
    val explicitClasses: Seq[VocabClassTerm] = allClasses.filter(_.id.startsWith(vocabulary.base)).distinct
    val explicitClassesMap = explicitClasses.foldLeft(Map[String, VocabClassTerm]()) {
      case (acc, klass) =>
        acc + (klass.id -> klass)
    }

    // Classes in this vocabulary because they are used in extension from other classes
    val implicitClassesExtension: Seq[String] = allClasses.flatMap { klass =>
      klass.superClasses.filter { superClass =>
        superClass.startsWith(vocabulary.base) && !explicitClassesMap.contains(superClass)
      }
    }

    // Implicit IDs -> simple class terms
    val implicitClasses = (implicitClassesExtension ++ implicitClassesExtension).distinct.map { klassId =>
      VocabClassTerm(klassId, uriAlias(klassId), "", Seq(), Seq())
    }

    // final set of classes
    (explicitClasses ++ implicitClasses).sortBy(_.id)
  }

  def usesReferences(vocabulary: ModelVocabulary,
                     classTerms: Seq[VocabClassTerm],
                     propertyTerms: Seq[VocabPropertyTerm]): Seq[ModelVocabulary] = {
    val classExtendsRefs = classTerms.flatMap { klass =>
      klass.superClasses.filter(!_.startsWith(vocabulary.base))
    }
    val propertiesExtendsRefs = propertyTerms.flatMap { property =>
      property.superClasses.filter(!_.startsWith(vocabulary.base))
    }
    val propertiesRangeRefs = propertyTerms.flatMap { property =>
      property.objectRange.filter { range =>
        !conflictive.contains(range) && !range.startsWith(vocabulary.base)
      }
    }

    val vocabularies = (classExtendsRefs ++ propertiesExtendsRefs ++ propertiesRangeRefs).map { id =>
      findNamespace(id) match {
        case Some(vocab) => vocab
        case None =>
          throw new Exception(s"Cannot find vocabulary for URI term $id")
      }
    }

    vocabularies.distinct
  }

  def metaObjects(reflections: Reflections, handler: String => Any): Unit = {
    reflections.getAllTypes.forEach { className =>
      if (className.endsWith("$")) {
        handler(className)
      }
    }
  }

  @tailrec
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
        if (propertyTerm.id != DomainElementModel.CustomDomainProperties.value.iri()) {
          val id = other.`type`.head.iri()
          propertyTerm.copy(objectRange = Some(id))
        } else {
          propertyTerm
        }
    }
  }

  def fieldToVocabProperty(field: Field): VocabPropertyTerm = {
    val id          = field.value.iri()
    val doc         = field.doc
    val displayName = doc.displayName
    val description = doc.description

    val propertyTerm = VocabPropertyTerm(id, displayName, description, doc.superClasses, None, None, Set())

    computeRange(field.`type`, propertyTerm)
  }

  def buildPropertyTerm(field: Field, klass: VocabClassTerm): Unit = {
    val id = field.value.iri()
    val propertyTerm = properties.get(id) match {
      case Some(prop) => prop
      case None =>
        val propertyTerm = fieldToVocabProperty(field)
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
    val doc   = modelObject.doc
    val types = modelObject.`type`.map(_.iri())
    if (types.isEmpty) {
      None
    } else {
      var id          = types.head
      val displayName = doc.displayName
      val description = doc.description
      val vocab       = doc.vocabulary.filename

      val superClassesInDoc = types.tail
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

      var finalSuperclasses =
        (superClassesInDoc ++ superClassesInInhertiance).distinct.filter(!conflictive.contains(_))
      // We need to solve a problem with the main class for the ShapeModel
      var classTerm = if (klassName == "amf.core.metamodel.domain.ShapeModel$") {
        val shapesShape = (Namespace.Shapes + "Shape").iri()
        val tmp         = finalSuperclasses.filter(_ != shapesShape)
        finalSuperclasses = id :: tmp
        id = shapesShape
        VocabClassTerm(id = shapesShape,
                       displayName = displayName,
                       description = description,
                       superClasses = finalSuperclasses)
      } else {
        VocabClassTerm(id = id, displayName = displayName, description = description, superClasses = finalSuperclasses)
      }

      classes = classes + (id         -> classTerm)
      classToFile = classToFile + (id -> vocab)

      /// index fields

      val fields = if (id == DomainElementModel.`type`.head.iri()) {
        // Annotation propery is lazy, not connected in fields, we need to add it manually
        modelObject.fields ++ Seq(DomainElementModel.CustomDomainProperties)
      } else {
        // regular fields
        modelObject.fields
      }

      fields.foreach { field =>
        buildPropertyTerm(field, classTerm)
        classTerm = classes(id) // update after linking in property term
      }

      Some(classTerm)
    }
  }

  def parseMetaObject(klassName: String): Option[VocabClassTerm] = {
    classes.get(klassName) match {
      case cached @ Some(_) => cached
      case _ =>
        try {
          val singleton = Class.forName(klassName)
          singleton.getField("MODULE$").get(singleton) match {
            case modelObject: Obj =>
              buildClassTerm(klassName, modelObject)
            case _ =>
              //println(s"Other thing: $other")
              None
          }
        } catch {
          case _: ClassNotFoundException =>
            //println(s"NOT FOUND '${klassName}'")
            None
          case _: NoSuchFieldException =>
            //println(s"NOT FIELD '${klassName}'")
            None
        }
    }
  }

  def main(args: Array[String]): Unit = {

    println("*** Starting")

    // let's initialize the files
    fillInitialFiles()

    println("*** Processing classes")
    metaObjects(reflectionsExtensions, parseMetaObject)
    metaObjects(reflectionsCoreDoc, parseMetaObject)
    metaObjects(reflectionsCoreDomain, parseMetaObject)
    metaObjects(reflectionsWebApi, parseMetaObject)
    metaObjects(reflectionsWebApiDoc, parseMetaObject)
    metaObjects(reflectionsShapes, parseMetaObject)
    metaObjects(reflectionsTemplates, parseMetaObject)
    metaObjects(reflectionsVocabularies, parseMetaObject)
    metaObjects(reflectionsVocabDoc, parseMetaObject)
    metaObjects(reflectionsExtModel, parseMetaObject)

    // review
    println(s"*** Parsed classes: ${classes.keys.toSeq.size}")
    //classes.keys.toSeq.sorted.foreach(k => println(s" - ${k}"))

    println(s"*** Parsed properties: ${properties.keys.toSeq.size}")
    //properties.keys.toSeq.sorted.foreach(k => println(s" - ${k}"))

    (Seq(
      ModelVocabularies.AmlDoc,
      ModelVocabularies.ApiBinding,
      ModelVocabularies.ApiContract,
      ModelVocabularies.Core,
      ModelVocabularies.Data,
      ModelVocabularies.Shapes,
      ModelVocabularies.Security,
      ModelVocabularies.Meta
    ) ++
      ExternalModelVocabularies.all).foreach { vocab =>
      println(s"**** RENDERING ${vocab.filename}")
      val path = if (ExternalModelVocabularies.all.contains(vocab)) {
        s"vocabularies/vocabularies/external/${vocab.filename}"
      } else {
        s"vocabularies/vocabularies/${vocab.filename}"
      }
      val f      = new File(path)
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
