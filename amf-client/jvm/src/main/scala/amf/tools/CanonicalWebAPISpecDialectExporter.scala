package amf.tools

import java.io.{File, FileWriter}

import amf.core.metamodel.Type.Scalar
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.vocabulary.Namespace
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner

import scala.collection.mutable

case class ExtendedDialectNodeMapping(id: String, name: String, classTerm: String, extended: Seq[String], propertyMappings: List[DialectPropertyMapping], isShape: Boolean)

object CanonicalWebAPISpecDialectExporter {
  val DIALECT_FILE = "canonical_webapi_spec.yaml"
  val WELL_KNOWN_VOCABULARIES = Map[String,String](
    "http://a.ml/vocabularies/document#" -> "../vocabularies/aml_doc.yaml",
    "http://a.ml/vocabularies/data#" -> "../vocabularies/data_model.yaml",
    "http://a.ml/vocabularies/apiContract#" -> "../vocabularies/api_contract.yaml",
    "http://a.ml/vocabularies/core#" -> "../vocabularies/core.yaml",
    "http://a.ml/vocabularies/meta#" -> "../vocabularies/aml_meta.yaml",
    "http://a.ml/vocabularies/security#" -> "../vocabularies/security.yaml",
    "http://a.ml/vocabularies/shapes#" -> "../vocabularies/data_shapes.yaml"
  )

  val reflectionsWebApi     = new Reflections("amf.plugins.domain.webapi.metamodel", new SubTypesScanner(false))
  val reflectionsShapes     = new Reflections("amf.plugins.domain.shapes.metamodel", new SubTypesScanner(false))
  val reflectionsCore       = new Reflections("amf.core.metamodel.domain.extensions", new SubTypesScanner(false))
  val reflectionsTemplates  = new Reflections("amf.core.metamodel.domain.templates", new SubTypesScanner(false))
  val reflectionsDataNode   = new Reflections("amf.core.metamodel.domain", new SubTypesScanner(false))
  val reflectionsApiDocs    = new Reflections("amf.plugins.document.webapi.metamodel", new SubTypesScanner(false))
  val reflectionsDocs       = new Reflections("amf.core.metamodel.document", new SubTypesScanner(false))

  var nodeMappings: Map[String, ExtendedDialectNodeMapping] = Map()

  def toCamelCase(str: String): String = {
    // str.replace(" ", "")
    val words = str.split("\\s+")
    if (words.size == 1) {
      words.head
    } else {
      (Seq(words.head) ++ words.tail.map(_.capitalize)).mkString
    }
  }

  def metaTypeToDialectRange(metaType: Type): (String, Boolean) = {
    metaType match {
      // objects
      case metaModel:Obj                               => (toCamelCase(metaModel.doc.displayName), false)
      // scalars
      case scalar: Scalar if scalar == Type.Str        => ("string", false)
      case scalar: Scalar if scalar == Type.RegExp     => ("string", false)
      case scalar: Scalar if scalar == Type.Int        => ("integer", false)
      case scalar: Scalar if scalar == Type.Float      => ("float", false)
      case scalar: Scalar if scalar == Type.Double     => ("double", false)
      case scalar: Scalar if scalar == Type.Time       => ("time", false)
      case scalar: Scalar if scalar == Type.Date       => ("date", false)
      case scalar: Scalar if scalar == Type.DateTime   => ("dateTime", false)
      case scalar: Scalar if scalar == Type.Iri        => ("uri", false)
      case scalar: Scalar if scalar == Type.EncodedIri => ("uri", false)
      case scalar: Scalar if scalar == Type.Bool       => ("boolean", false)
      // collections
      case Type.Array(t)                               => (metaTypeToDialectRange(t)._1, true)
      case Type.SortedArray(t)                         => (metaTypeToDialectRange(t)._1, true)
    }
  }

  def buildPropertyMapping(field: Field): DialectPropertyMapping = {
    val propertyTerm          = field.value.iri()
    val name = toCamelCase(field.doc.displayName).replace("\\.", "")
    val (range, allowMultiple) = metaTypeToDialectRange(field.`type`)

    DialectPropertyMapping(name, propertyTerm, range, allowMultiple)
  }

  def buildNodeMapping(klassName: String, modelObject: Obj): ExtendedDialectNodeMapping = {
    val doc         = modelObject.doc
    val types       = modelObject.`type`.map(_.iri())
    val id          = types.head
    val displayName = doc.displayName
    val description = doc.description
    val vocab       = doc.vocabulary.filename


    // index fields
    val fields = if (types.contains((DomainElementModel.`type`.head.iri())) && displayName != "CustomDomainProperty") {
      modelObject.fields ++ Seq(DomainElementModel.CustomDomainProperties) // domain elements can have annotations
    } else {
      modelObject.fields
    }
    val propertyTerms = fields.map(buildPropertyMapping)
    val isShape = types.contains((Namespace.Shacl + "Shape").iri())

    // Find superSchemas
    val superSchemas =
      if (id == (Namespace.Document + "Document").iri()) {
        Seq(
          (Namespace.Document + "Unit").iri()
        )
      } else if (id == (Namespace.Shapes + "AnyShape").iri()) {
        Seq(
          (Namespace.Shacl + "Shape").iri(),
        )
      } else if (id == (Namespace.Shapes + "RecursiveShape").iri()) {
        Seq(
          (Namespace.Shacl + "Shape").iri(),
        )
      } else if (id == (Namespace.Shacl + "PropertyShape").iri()) {
        Seq(
          (Namespace.Shacl + "Shape").iri(),
        )
      } else if (id == (Namespace.Shapes + "NilShape").iri()) {
        Seq(
          (Namespace.Shapes + "AnyShape").iri(),
        )
      } else {
        // we drop the first one (this schema) and then get the first not blacklisted.
        // we relay in the list of super types being defined by more important to less specific type
        types.drop(1).find(!blacklistedSupertypes.contains(_)).map(Seq(_)).getOrElse(Nil)
      }

    val nodeMapping = ExtendedDialectNodeMapping(id = id, name = toCamelCase(displayName), classTerm = id, extended = superSchemas, propertyMappings = propertyTerms, isShape = isShape)

    nodeMapping
  }

  def parseMetaObject(klassName: String): Option[ExtendedDialectNodeMapping] = {
    nodeMappings.get(klassName) match {
      case cached @ Some(_) => cached
      case _                =>
        try {
          val singleton = Class.forName(klassName)
          singleton.getField("MODULE$").get(singleton) match {
            case modelObject: Obj =>
              val nodeMapping = buildNodeMapping(klassName, modelObject)
              nodeMappings += (klassName -> nodeMapping)
              Some(nodeMapping)
            case other =>
              // println(s"Other thing: $other")
              None
          }
        } catch {
          case _: ClassNotFoundException =>
            // println(s"NOT FOUND '${klassName}'")
            None
          case _: NoSuchFieldException =>
            // println(s"NOT FIELD '${klassName}'")
            None
        }
    }
  }

  def allowedShapePropertyMapping(propertyTerm: String, isBaseShape: Boolean): Boolean = {
    if (isBaseShape) {
      val shapeMapping = nodeMappings("amf.core.metamodel.domain.ShapeModel$")
      val duplicatedPropertyFound = shapeMapping.propertyMappings.find { p =>
        p.propertyTerm == propertyTerm
      }
      duplicatedPropertyFound.isEmpty
    } else {
      true
    }
  }

  def allowedAnyShapePropertyMapping(propertyTerm: String, isAnyShape: Boolean): Boolean = {
    if (isAnyShape) {
      val shapeMapping = nodeMappings("amf.plugins.domain.shapes.metamodel.AnyShapeModel$")
      val duplicatedPropertyFound = shapeMapping.propertyMappings.find { p =>
        p.propertyTerm == propertyTerm
      }
      duplicatedPropertyFound.isEmpty
    } else {
      true
    }
  }

  def cleanInheritance(): Unit = {
    val cleanNodeMappings: mutable.Map[String, ExtendedDialectNodeMapping] = mutable.Map()
    nodeMappings = nodeMappings.foldLeft(Map[String, ExtendedDialectNodeMapping]()) { case (acc, (old, mapping)) =>
        acc.updated(mapping.id, mapping)//.updated(old, mapping)
    }
    nodeMappings.foreach { case (id, mapping) =>
      val superMappings = findExtended(mapping)
      val inheritedProperties = superMappings.map(nodeMappings(_)).flatMap(_.propertyMappings)
      val inheritedPropertiesMap = inheritedProperties.foldLeft(Set[String]()) { case (acc, p) =>
        acc + p.name
      }
      val filteredPropertyMappings = mapping.propertyMappings.filter(p => !inheritedPropertiesMap.contains(p.name))
      cleanNodeMappings.update(id, mapping.copy(propertyMappings = filteredPropertyMappings))
    }
    nodeMappings = cleanNodeMappings.toMap
  }


  def findExtended(mapping: ExtendedDialectNodeMapping): Seq[String] = {
    val collected = mapping.extended.flatMap { superMappingId =>
      nodeMappings.get(superMappingId) match {
        case Some(superMapping: ExtendedDialectNodeMapping) =>
          Seq(superMapping.id) ++ findExtended(superMapping)
        case _                  => Nil
      }
    }

    collected.distinct
  }

  val blacklistedProperties: Set[String] = Set(
    (Namespace.Document + "extends").iri(),
    (Namespace.Document + "link-target").iri(),
    (Namespace.Document + "link-label").iri(),
    (Namespace.Document + "recursive").iri()
  )

  val blacklistedSupertypes: Set[String] = Set(
    (Namespace.Document + "DomainElement").iri(),
    (Namespace.Document + "RootDomainElement").iri(),
    (Namespace.Shacl + "Shape").iri(),
    (Namespace.Shapes + "Shape").iri(),
    (Namespace.Rdf + "Property").iri(),
    (Namespace.Rdf +  "Seq").iri()
  )

  val blacklistedRanges: Set[String] = Set(

  )

  // Base classes that should not appear in the dialect
  val blacklistedMappings: Set[String] = Set(
    "LinkableElement",
    "DomainElement",
    "SourceMap"
  )

  val shapeUnionDeclaration = "DataShapesUnion"

  val shapeTypeDiscriminator =
    """    typeDiscriminatorName: shapeType
      |    typeDiscriminator:
      |      Union: UnionShape
      |      Tuple: TupleShape
      |      Node: NodeShape
      |      Array: ArrayShape
      |      Schema: SchemaShape
      |      File: FileShape
      |      Nil: NilShape
      |      Scalar: ScalarShape
      |      Any: AnyShape
      |      Recursive: RecursiveShape
    """.stripMargin

  val shapeUnionRange =
    """      - UnionShape
      |      - TupleShape
      |      - NodeShape
      |      - ArrayShape
      |      - SchemaShape
      |      - FileShape
      |      - MatrixShape
      |      - NilShape
      |      - ScalarShape
      |      - AnyShape
      |      - RecursiveShape
    """.stripMargin


  val settingsUnionDeclaration = "SecuritySettingsUnion"

  val settingsTypeDiscriminator =
    """    typeDiscriminatorName: settingsType
      |    typeDiscriminator:
      |      OAuth2: OAuth2Settings
      |      OAuth1: OAuth1Settings
      |      APIKey: APIKeySettings
      |      Http: HTTPSettings
      |      OpenID: OpenIDSettings
    """.stripMargin

  val settingsUnionRange =
    """      - OAuth2Settings
      |      - OAuth1Settings
      |      - APIKeySettings
      |      - HTTPSettings
      |      - OpenIDSettings
    """.stripMargin

  val abstractDeclarationsRange =
    """          - ResourceType
      |          - Trait
    """.stripMargin

  val declarations =
    s"""    declares:
       |      dataShapes: $shapeUnionDeclaration
       |      resourceTypes: ResourceType
       |      traits: Trait
  """.stripMargin

  val customDomainProperty =
    """
      |      customDomainProperties:
      |        propertyTerm: doc.customDomainProperties
      |        range: CustomDomainProperty
      |""".stripMargin

  val endPointExtends =
    """      extends:
      |        propertyTerm: doc.extends
      |        typeDiscriminatorName: type
      |        typeDiscriminator:
      |          AppliedResourceType: ParametrizedResourceType
      |          AppliedTrait: ParametrizedTrait
      |        range:
      |          - ParametrizedResourceType
      |          - ParametrizedTrait
      |        allowMultiple: true
    """.stripMargin

  val operationExtends =
    """      extends:
      |        propertyTerm: doc.extends
      |        typeDiscriminatorName: type
      |        typeDiscriminator:
      |          AppliedTrait: ParametrizedTrait
      |        range:
      |          - ParametrizedTrait
      |        allowMultiple: true
    """.stripMargin

  val dataNodeUnionDeclaration = "DataNodeUnion"
  val dataNodeUnion =
    s"""
       |  $dataNodeUnionDeclaration:
       |    typeDiscriminatorName: elementType
       |    typeDiscriminator:
       |      Scalar: ScalarNode
       |      Array: ArrayNode
       |      Link: LinkNode
       |      Object: ObjectNode
       |
       |    union:
       |      - ScalarNode
       |      - ArrayNode
       |      - LinkNode
       |      - ObjectNode
       |""".stripMargin

  val domainElementUnionDeclaration = "DomainElementUnion"
  val domainElementUnion =
    s"""
      |  $domainElementUnionDeclaration:
      |    typeDiscriminatorName: elementType
      |    typeDiscriminator:
      |      Union: UnionShape
      |      Tuple: TupleShape
      |      Node: NodeShape
      |      Array: ArrayShape
      |      Schema: SchemaShape
      |      File: FileShape
      |      Nil: NilShape
      |      Scalar: ScalarShape
      |      Any: AnyShape
      |      Recursive: RecursiveShape
      |      OAuth2: OAuth2Settings
      |      SecurityScheme: SecurityScheme
      |      Parameter: Parameter
      |      Request: Request
      |      Callback: Callback
      |      Tag: Tag
      |      WebAPI: WebAPI
      |      Example: Example
      |      Trait: Trait
      |      TemplatedLink: TemplatedLink
      |      Server: Server
      |      ResourceType: ResourceType
      |
      |    union:
      |      - UnionShape
      |      - TupleShape
      |      - NodeShape
      |      - ArrayShape
      |      - SchemaShape
      |      - FileShape
      |      - MatrixShape
      |      - NilShape
      |      - ScalarShape
      |      - AnyShape
      |      - RecursiveShape
      |      - SecurityScheme
      |      - Parameter
      |      - Request
      |      - Callback
      |      - Tag
      |      - WebAPI
      |      - Example
      |      - Trait
      |      - TemplatedLink
      |      - Server
      |      - ResourceType
      |""".stripMargin

  val parsedUnitUnionDeclaration = "ParsedUnitUnion"
  val parsedUnitUnion =
    s"""  $parsedUnitUnionDeclaration:
      |    typeDiscriminatorName: unitType
      |    typeDiscriminator:
      |      AnnotationTypeFragment: AnnotationTypeFragment
      |      DataTypeFragment: DataTypeFragment
      |      DocumentationItemFragment: DocumentationItemFragment
      |      NamedExampleFragment: NamedExampleFragment
      |      ResourceTypeFragment: ResourceTypeFragment
      |      TraitFragment: TraitFragment
      |      SecuritySchemeFragment: SecuritySchemeFragment
      |      ExternalFragment: ExternalFragment
      |      Library: Module
      |      Document: Document
      |      Extension: Extension
      |      Overlay: OverlayModel
      |    union:
      |      - DataTypeFragment
      |      - DocumentationItemFragment
      |      - NamedExampleFragment
      |      - ResourceTypeFragment
      |      - TraitFragment
      |      - SecuritySchemeFragment
      |      - Module
      |      - Document
      |      - Extension
      |      - OverlayModel
      |""".stripMargin

  def renderDialect(): String = {
    val stringBuilder = new StringBuilder()
    val externals: mutable.HashMap[String,String] = mutable.HashMap()
    val header = "#%Dialect 1.0\n\n" ++
      "dialect: WebAPI\n" ++
      "version: 1.0\n\n"

    // Shapes union
    stringBuilder.append(s"  $shapeUnionDeclaration:\n")
    stringBuilder.append(shapeTypeDiscriminator + "\n")
    stringBuilder.append("    union:\n")
    stringBuilder.append(shapeUnionRange + "\n")

    // Security settings union
    stringBuilder.append(s"  $settingsUnionDeclaration:\n")
    stringBuilder.append(settingsTypeDiscriminator + "\n")
    stringBuilder.append("    union:\n")
    stringBuilder.append(settingsUnionRange + "\n")

    // data node union
    stringBuilder.append(dataNodeUnion + "\n")

    // domain element union
    stringBuilder.append(domainElementUnion + "\n")

    // Parsed unit union
    stringBuilder.append(parsedUnitUnion + "\n")

    val orderedNodeMappings = nodeMappings.values.toSeq.sortBy(_.name).map(_.id)
    orderedNodeMappings.foreach { nodeMappingId =>
      nodeMappings.get(nodeMappingId) match {
        case Some(dialectNodeMapping: ExtendedDialectNodeMapping) =>
          if (!blacklistedMappings.contains(dialectNodeMapping.name)) {
            stringBuilder.append(s"  ${dialectNodeMapping.name}:\n")
            var (compacted, prefix, base) = compact(dialectNodeMapping.classTerm)
            aggregateExternals(externals, prefix, base)
            stringBuilder.append(s"    classTerm: $compacted\n")

            // Lets find the effective property mappings for this node mapping
            val nodeMappingWithProperties = dialectNodeMapping.propertyMappings.filter { propertyMapping =>
              // dynamic and linking information only relevant for design will not be dumped in the dialect
              !blacklistedProperties.contains(propertyMapping.propertyTerm) &&
                !blacklistedRanges.contains(propertyMapping.range)
            }

            if (dialectNodeMapping.extended.nonEmpty) {
              if (dialectNodeMapping.extended.length == 1) {
                stringBuilder.append(s"    extends: ${nodeMappings(dialectNodeMapping.extended.head).name}\n")
              } else {
                stringBuilder.append(s"    extends:\n")
                dialectNodeMapping.extended.foreach { id =>
                  stringBuilder.append(s"      - ${nodeMappings(id).name}\n")
                }
              }
            }

            if (nodeMappingWithProperties.nonEmpty) {

              var propertyCounters: mutable.Map[String, Int] = mutable.Map() // for properties with dupplicated labels

              stringBuilder.append(s"    mapping:\n")

              if (dialectNodeMapping.classTerm == (Namespace.ApiContract + "EndPoint").iri()) {
                stringBuilder.append(endPointExtends + "\n")
              } else if (dialectNodeMapping.classTerm == (Namespace.ApiContract + "Operation").iri()) {
                stringBuilder.append(operationExtends + "\n")
              }


              nodeMappingWithProperties.map { propertyMapping =>
                // property names can be duplicated in the WebAPI meta-model, we make sure
                // we generate unique property mapping alias
                val name = propertyMapping.name
                val nextPropertyName = propertyCounters.get(name) match {
                  case None =>
                    propertyCounters.update(name, 1)
                    name
                  case Some(counter) =>
                    propertyCounters.update(name, counter + 1)
                    s"$name$counter"
                }
                // render the property mapping here
                stringBuilder.append(s"      $nextPropertyName:\n")
                var (compacted, prefix, base) = compact(propertyMapping.propertyTerm)
                aggregateExternals(externals, prefix, base)
                stringBuilder.append(s"        propertyTerm: $compacted\n")
                if (propertyMapping.range == "Shape") {
                  stringBuilder.append(s"        range: $shapeUnionDeclaration\n")
                } else if (propertyMapping.range == "Settings") {
                  stringBuilder.append(s"        range: $settingsUnionDeclaration\n")
                } else if (propertyMapping.range == "AbstractDeclaration") {
                  stringBuilder.append(s"        range:\n")
                  stringBuilder.append(abstractDeclarationsRange ++ "\n")
                } else if (propertyMapping.range == "DomainElement") {
                  stringBuilder.append(s"        range: $domainElementUnionDeclaration\n")
                } else if (propertyMapping.range == "BaseUnit") {
                  stringBuilder.append(s"        range: $parsedUnitUnionDeclaration\n")
                } else if (propertyMapping.range == "DataNode") {
                  stringBuilder.append(s"        range: $dataNodeUnionDeclaration\n")
                } else if (propertyMapping.range == "uri") {
                  stringBuilder.append(s"        range: link\n")
                } else {
                  stringBuilder.append(s"        range: ${propertyMapping.range}\n")
                }
                if (propertyMapping.allowMultiple) {
                  stringBuilder.append(s"        allowMultiple: ${propertyMapping.allowMultiple}\n")
                }

              }
            }
            stringBuilder.append("\n\n")
          }
      }
    }

    stringBuilder.append("\n\n")
    stringBuilder.append("documents:\n")
    stringBuilder.append("  root:\n")
    stringBuilder.append(s"    encodes: ${parsedUnitUnionDeclaration}\n")
    // TODO: union of declarations
    // stringBuilder.append(declarations)

    val effectiveExternals = externals.filter { case (p, b) =>
      !WELL_KNOWN_VOCABULARIES.contains(b)
    }
    val effectiveVocabularies = externals.filter { case (p, b) =>
      WELL_KNOWN_VOCABULARIES.contains(b)
    } map { case (p, b) =>
      p -> WELL_KNOWN_VOCABULARIES(b)
    }

    val vocabularyDepedencies = "uses:\n" ++ effectiveVocabularies.map { case (p, b) => s"  $p: $b\n" }.mkString ++ "\n"
    val externalDependencies = "external:\n" ++ effectiveExternals.map { case (p, b) => s"  $p: $b\n" }.mkString ++ "\n"

    header ++ vocabularyDepedencies ++ externalDependencies ++ "nodeMappings:\n\n" ++ stringBuilder.mkString
  }

  def aggregateExternals(externals: mutable.HashMap[String, String], prefix: String, base: String): Unit = {
    if (!externals.contains(prefix)) {
      externals.update(prefix, base)
    }
  }

  def compact(url: String): (String, String, String) = {
    val compacted = Namespace.compact(url).replace(":", ".")
    val prefix = compacted.split("\\.").head
    val base = Namespace.ns(prefix).base
    (compacted, prefix, base)
  }

  def main(args: Array[String]): Unit = {

    val f      = new File(s"vocabularies/dialects/${DIALECT_FILE}")
    val writer = new FileWriter(f)
    try {
      println("*** Processing classes")
      VocabularyExporter.metaObjects(reflectionsWebApi, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsShapes, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsCore, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsTemplates, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsDataNode, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsApiDocs, parseMetaObject)
      VocabularyExporter.metaObjects(reflectionsDocs, parseMetaObject)
      cleanInheritance()
      val dialectText = renderDialect()
      println(dialectText)
      writer.write(dialectText)
    } finally {
      writer.close()
    }
  }
}
