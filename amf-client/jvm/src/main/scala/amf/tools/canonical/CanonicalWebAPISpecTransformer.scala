package amf.tools.canonical

import amf.core.metamodel.Obj
import amf.core.metamodel.document.{DocumentModel, ExternalFragmentModel, ModuleModel}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.plugin.{CorePlugin, PluginContext}
import amf.core.rdf.RdfModelParser
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.metamodel.domain.NodeMappingModel
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.document.webapi.Raml10Plugin
import amf.plugins.document.webapi.metamodel.{ExtensionModel, OverlayModel}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.tools.canonical.JenaUtils.all
import amf.tools.{CanonicalWebAPISpecDialectExporter, PropertyNodeModel}
import org.apache.jena.rdf.model.{Model, RDFNode, Resource, Statement}

import scala.collection.mutable
import scala.concurrent.Future

object CanonicalWebAPISpecTransformer extends PlatformSecrets {
  type DomainElementUri = String
  type TypeUri          = String
  type DialectNode      = String

  val CANONICAL_WEBAPI_NAME = "WebAPI Spec 1.0"

  // Properties that will be inserted in the graph
  val REPO_INTERNAL_REF   = "http://anypoint.com/vocabs/digital-repository#internalReference"
  val REPO_ASSET_LOCATION = "http://anypoint.com/vocabs/digital-repository#location"
  val REPO_LINK_TARGET    = "http://anypoint.com/vocabs/digital-repository#link-target"
  val REPO_LINK_LABEL     = "http://anypoint.com/vocabs/digital-repository#link-label"
  val REPO_EXTENDS        = "http://anypoint.com/vocabs/digital-repository#extends"

  protected def findWebAPIDialect: Option[Dialect] =
    AMLPlugin().registry.allDialects().find(_.nameAndVersion() == CANONICAL_WEBAPI_NAME)

  /**
    * Creates a map from the node mapping in the canonical web api dialect to the mapped
    * class in the WebAPI vocabulary
    * @return
    */
  protected def buildCanonicalClassMapping: Map[TypeUri, DialectNode] = {
    findWebAPIDialect match {
      case Some(webApiDialect) =>
        val nodeMappings = webApiDialect.declares.collect { case mapping: NodeMapping => mapping }
        nodeMappings.foldLeft(Map[String, String]()) {
          case (acc, mapping) =>
            acc + (mapping.nodetypeMapping.value() -> mapping.id)
        }
      case None =>
        throw new Exception("Cannot find WebAPI 1.0 Dialect in Dialect registry")
    }
  }

  /**
    * Renames the URI of a resource node in the RDF graph: incoming and outgoing edges
    */
  def renameResource(source: String, target: String, nativeModel: Model): Unit = {
    // remove matching S(source)-p-o statements, add S(target)-p-o statements
    val subjectsIterator = nativeModel.listStatements(nativeModel.createResource(source), null, null)
    val acc              = mutable.ArrayBuffer[Statement]()
    while (subjectsIterator.hasNext) {
      val nextStatement = subjectsIterator.nextStatement()
      acc += nextStatement
    }
    acc.foreach { st =>
      nativeModel.remove(st)
      val updatedStatement = nativeModel.createStatement(
        nativeModel.createResource(target),
        st.getPredicate,
        st.getObject
      )
      nativeModel.add(updatedStatement)
    }

    // remove matching s-p-O(source) statements, add s-p-O(target) statements
    val objectsIterator = nativeModel.listStatements(null, null, nativeModel.createResource(source))
    acc.clear()
    while (objectsIterator.hasNext) {
      val nextStatement = objectsIterator.nextStatement()
      acc += nextStatement
    }
    acc.foreach { st =>
      nativeModel.remove(st)
      val updatedStatement = nativeModel.createStatement(
        st.getSubject,
        st.getPredicate,
        nativeModel.createResource(target)
      )
      nativeModel.add(updatedStatement)
    }
  }

  private def mapBaseUnits(unit: String, dialect: Dialect, nativeModel: Model) = {
    val unitResource = nativeModel.createResource(unit)

    // Process and remove old types

    val allTypesIterator = nativeModel.listObjectsOfProperty(
      unitResource,
      nativeModel.createProperty((Namespace.Rdf + "type").iri())
    )
    val allTypes = mutable.ArrayBuffer[String]()
    while (allTypesIterator.hasNext) {
      allTypes += allTypesIterator.next().asResource().getURI
    }

    val mappedDocumentType = if (allTypes.contains((Namespace.ApiContract + "Extension").iri())) {
      ExtensionModel.`type`.head.iri()
    } else if (allTypes.contains((Namespace.ApiContract + "Overlay").iri())) {
      // (Namespace.ApiContract + "WebAPIDocument").iri()
      OverlayModel.`type`.head.iri()
    } else if (allTypes.contains((Namespace.Document + "Document").iri())) {
      // (Namespace.ApiContract + "WebAPIDocument").iri()
      DocumentModel.`type`.head.iri()
    } else if (allTypes.contains((Namespace.Document + "Module").iri())) {
      //(Namespace.ApiContract + "LibraryModule").iri()
      ModuleModel.`type`.head.iri()
    } else if (allTypes.contains((Namespace.Document + "ExternalFragment").iri())) {
      // (Namespace.Document + "ExternalFragment").iri()
      ExternalFragmentModel.`type`.head.iri()
    } else {
      val cleanTypes = allTypes.filter { t =>
        t != (Namespace.Document + "Unit").iri() &&
        t != (Namespace.Document + "Document").iri() &&
        t != (Namespace.Document + "Fragment").iri()
      }
      cleanTypes.head
    }

    dialect.declares.find { nodeMapping =>
      nodeMapping.id.split("#").last.split("/").last == mappedDocumentType.split("#").last
    } match {
      case Some(nodeMapping) =>
        nativeModel.remove(
          unitResource,
          nativeModel.createProperty((Namespace.Rdf + "type").iri()),
          nativeModel.createResource(nodeMapping.id)
        )
      case _ =>
        println(s"Couldn't find node mapping for $mappedDocumentType")
    }

    // now is a dialect domain element
    nativeModel.add(
      unitResource,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
    )
    // still a domain element, TODO: do we need this one?
    nativeModel.add(
      unitResource,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "DomainElement").iri())
    )

//    println(s"Generated TYPE: $mappedDocumentType")

    // remove the old types
    allTypes.foreach { t =>
      nativeModel.remove(
        unitResource,
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource(t)
      )
    }
    // ad the new asset type property
    nativeModel.add(
      unitResource,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource(mappedDocumentType)
    )

    // add the new asset location property
    nativeModel.add(
      unitResource,
      nativeModel.createProperty(REPO_ASSET_LOCATION),
      nativeModel.createLiteral(unit)
    )

    // add the extendedFrom property to track baseunit node extension
    dialect.declares.find(_.id.endsWith("ParsedUnit")) match {
      case Some(parsedUnitNode) =>
        nativeModel.add(
          unitResource,
          nativeModel.createProperty(NodeMappingModel.ResolvedExtends.value.iri()),
          nativeModel.createLiteral(parsedUnitNode.id)
        )
      case _ => // ignore
    }
  }

  /**
    * Transforms the doc:Units in the graph into the domain elements for assets in the canonical web api spec dialect
    */
  def preProcessUnits(nativeModel: Model): String = {
    // we need the dialect ID in this particular instance of AMF
    val dialect = findWebAPIDialect.get

    // we save the top level document we will not transform into a domain level asset
    val topLevelUnit = nativeModel
      .listSubjectsWithProperty(
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource((Namespace.Document + "Document").iri())
      )
      .next()
      .getURI

    // get all the document units
    val unitsIterator = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Unit").iri())
    )
    val unitUris = mutable.ArrayBuffer[String]()
    while (unitsIterator.hasNext) {
      unitUris += unitsIterator.nextResource().getURI
    }

    // for each document unit that is not the root one, we transform that into the canonical webpi spec asset fragment node
    // defined in the SpecDocument node mapping schema
    unitUris.foreach { unit =>
      val unitResource = nativeModel.createResource(unit)

      // Let's manipulate the @type of the unit to match the dialect expectations
      mapBaseUnits(unit, dialect, nativeModel)

    }

    // we introduce a new top level document with the URI of the old top level document
    // we rename the old top level document (now a domain element)
    val encodedElement = topLevelUnit + "#/rootAsset" // picking this URI is safe, not present in the model
    // rename so we can reuse the old main root URI
    renameResource(topLevelUnit, encodedElement, nativeModel)
    val topLevel = nativeModel.createResource(topLevelUnit)
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Meta + "DialectInstance").iri())
    )
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Document").iri())
    )
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Fragment").iri())
    )
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Module").iri())
    )
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Unit").iri())
    )
    // connect the new root with the old root encoded as a domain element
    nativeModel.add(
      topLevel,
      nativeModel.createProperty((Namespace.Document + "encodes").iri()),
      nativeModel.createResource(encodedElement)
    )

    topLevelUnit
  }

  protected def transformDataNodes(typeMapping: Map[TypeUri, DialectNode], nativeModel: Model): Unit = {
    // we first remove the name from al ldata nodes
    // we first list all the data nodes
    val toCleanIt = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource(DataNodeModel.`type`.head.iri())
    )
    while (toCleanIt.hasNext) {
      val nextToClean = toCleanIt.next()
      val namesIt =
        nativeModel.listObjectsOfProperty(nextToClean, nativeModel.createProperty(DataNodeModel.Name.value.iri()))
      if (namesIt.hasNext) {
        nativeModel.remove(
          nativeModel.createStatement(
            nextToClean,
            nativeModel.createProperty(DataNodeModel.Name.value.iri()),
            namesIt.next()
          ))
      }
    }

    // Now we add the reified properties to dynamic object nodes
    val dataNodesIt = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource(ObjectNodeModel.`type`.head.iri())
    )

    while (dataNodesIt.hasNext) {
      val nextDataNode = dataNodesIt.nextResource()
      val propertiesIt = nativeModel.listStatements(nextDataNode, null, null)
      val props        = mutable.ListBuffer[(Resource, RDFNode)]()
      while (propertiesIt.hasNext) {
        val nextStatement = propertiesIt.next()
        val nextProperty  = nextStatement.getPredicate.asResource()
        val nextValue     = nextStatement.getObject
        if (nextProperty.getURI.startsWith(Namespace.Data.base)) {
          props.+=((nextProperty, nextValue))
        }
      }

      props.foreach {
        case (p, v) =>
          val name  = p.getURI.split("#").last.split("/").last
          val pReif = nativeModel.createResource(nextDataNode.getURI + "_prop_" + name)

          // link parent node and the reified property
          nativeModel.add(
            nextDataNode,
            nativeModel.createProperty(CanonicalWebAPISpecDialectExporter.DataPropertiesField.value.iri()),
            pReif
          )

          // name
          nativeModel.add(
            pReif,
            nativeModel.createProperty(DataNodeModel.Name.value.iri()),
            nativeModel.createLiteral(name)
          )

          // range
          nativeModel.add(
            pReif,
            nativeModel.createProperty(PropertyNodeModel.Range.value.iri()),
            v
          )

          // types
          nativeModel.add(
            pReif,
            nativeModel.createProperty((Namespace.Rdf + "type").iri()),
            nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
          )
          nativeModel.add(
            pReif,
            nativeModel.createProperty((Namespace.Rdf + "type").iri()),
            nativeModel.createResource(typeMapping(PropertyNodeModel.`type`.head.iri()))
          )
      }

      props.foreach {
        case (p, v) =>
          val toRemove = nativeModel.createStatement(nextDataNode, nativeModel.createProperty(p.getURI), v)
          nativeModel.remove(toRemove)
      }
    }
  }

  def domainElementsFrom(nativeModel: Model): Seq[DomainElementUri] = {
    val domainElements = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "DomainElement").iri())
    )

    val shapes = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Shapes + "Shape").iri())
    )

    all(domainElements) ++ all(shapes) map { _.getURI }
  }

  /**
    * Cleans the input WebAPI model adding the required information to the
    * underlying RDF graph and uses it to build the canonical WebAPI dialect
    * instance as output
    *
    * @param unit a AMF WebAPI model parsed from RAML / OAS
    * @return Equivalent Canonical WebAPI AML dialect instance
    */
  protected def cleanAMFModel(unit: BaseUnit): BaseUnit = {
    val typeMapping = buildCanonicalClassMapping
    val model       = unit.toNativeRdfModel()

    val nativeModel = model.native().asInstanceOf[Model]

    val baseUnitId = preProcessUnits(nativeModel)

    // First update document to DialectInstance document
    findWebAPIDialect match {
      case Some(dialect) =>
        nativeModel.add(
          nativeModel.createResource(baseUnitId),
          nativeModel.createProperty((Namespace.Meta + "definedBy").iri()),
          nativeModel.createResource(dialect.id)
        )
      case None => // ignore
    }

    // transform dynamic data nodes to list the properties
    transformDataNodes(typeMapping, nativeModel)

    transformDomainElements(typeMapping, nativeModel)

    val plugins = PluginContext(
      blacklist = Seq(CorePlugin, WebAPIDomainPlugin, DataShapesDomainPlugin, AMFGraphPlugin, Raml10Plugin))

    RdfModelParser(errorHandler = UnhandledParserErrorHandler, plugins = plugins)
      .parse(model, baseUnitId)
  }

  private def transformDomainElements(typeMapping: Map[TypeUri, DialectNode], nativeModel: Model): Unit = {
    domainElementsFrom(nativeModel).foreach { domainElement =>
      // we map types to dialect nodes and add them to the rdf:type property
      transformType(nativeModel, domainElement, typeMapping)

      // now we transform regular link-targets into design-link-targets so we can render them in a dialect without
      // triggering element dereference logic
      transformLink(nativeModel, domainElement)

      // same for custom domain properties
      // domain properties are generated as properties, now they will become
      // reified so we can list them
      transformAnnotations(nativeModel, typeMapping, domainElement)
    }
  }

  def transformType(nativeModel: Model, domainElement: DomainElementUri, mapping: Map[TypeUri, DialectNode]): Unit = {
    val typesIterator = nativeModel.listObjectsOfProperty(
      nativeModel.createResource(domainElement),
      nativeModel.createProperty((Namespace.Rdf + "type").iri())
    )

    // We need to deal with node shape inheritance
    // These flags allow us to track if we found any shape or shape in case
    // we cannot find a more specific shape
    var foundShape: Option[String]      = None
    var foundAnyShape: Option[String]   = None
    var foundArrayShape: Option[String] = None
    var found                           = false
    var mappedDialectNode               = ""
    while (typesIterator.hasNext) {
      val nextType = typesIterator.next().asResource().getURI
      mapping.get(nextType) match {
        case Some(dialectNode) =>
          // dealing with inheritance here
          if (!dialectNode.endsWith("#/declarations/Shape") && !dialectNode.endsWith("#/declarations/AnyShape") && !dialectNode
                .endsWith("#/declarations/DataNode") && !dialectNode.endsWith("#/declarations/ArrayShape")) {
            found = true
            mappedDialectNode = dialectNode
          } else if (dialectNode.endsWith("#/declarations/Shape")) {
            foundShape = Some(dialectNode)
          } else if (dialectNode.endsWith("#/declarations/AnyShape")) {
            foundAnyShape = Some(dialectNode)
          } else if (dialectNode.endsWith("#/declarations/ArrayShape")) {
            foundArrayShape = Some(dialectNode)
          }
        case _ => // ignore
      }
    }

    // Set the base shape node if we have find it and we didn't find anything more specific
    if (!found && foundArrayShape.isDefined) {
      mappedDialectNode = foundArrayShape.get
      found = true
    }
    if (!found && foundAnyShape.isDefined) {
      mappedDialectNode = foundAnyShape.get
      found = true
    }
    if (!found && foundShape.isDefined) {
      mappedDialectNode = foundShape.get
      found = true
    }

    nativeModel.add(
      nativeModel.createResource(domainElement),
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
    )
    nativeModel.add(
      nativeModel.createResource(domainElement),
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource(mappedDialectNode)
    )
  }

  protected def transformLink(nativeModel: Model, domainElement: DomainElementUri): Unit = {
    val linksIt = nativeModel.listObjectsOfProperty(
      nativeModel.createResource(domainElement),
      nativeModel.createProperty(LinkableElementModel.TargetId.value.iri())
    )
    while (linksIt.hasNext) {
      val nextLink = linksIt.next()
      nativeModel.remove(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty(LinkableElementModel.TargetId.value.iri()),
        nextLink
      )
      nativeModel.add(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty(CanonicalWebAPISpecDialectExporter.DesignLinkTargetField.value.iri()),
        nextLink
      )
    }
  }

  protected def transformAnnotations(nativeModel: Model,
                                     typeMapping: Map[TypeUri, DialectNode],
                                     domainElement: DomainElementUri) {
    val annotsIt = nativeModel.listObjectsOfProperty(
      nativeModel.createResource(domainElement),
      nativeModel.createProperty(DomainElementModel.CustomDomainProperties.value.iri())
    )
    var c = 0
    while (annotsIt.hasNext) {
      val nextAnnotation = annotsIt.next()
      nativeModel.remove(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty(DomainElementModel.CustomDomainProperties.value.iri()),
        nextAnnotation
      )
      // now we find the value of the annotation, the annotation property links to this value
      // directly in the annotated node
      val nextAnnotationValueIt = nativeModel.listObjectsOfProperty(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty(nextAnnotation.asResource().getURI)
      )
      val nextAnnotationValue = nextAnnotationValueIt.next()

      // autogen URI for the reified annotation
      val reifiedAnnotationUri = nativeModel.createResource(domainElement + s"_annotations_$c")
      c += 1

      nativeModel.add(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty(CanonicalWebAPISpecDialectExporter.DesignAnnotationField.value.iri()),
        reifiedAnnotationUri
      )
      nativeModel.add(
        reifiedAnnotationUri,
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
      )
      nativeModel.add(
        reifiedAnnotationUri,
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource(typeMapping(DomainExtensionModel.`type`.head.iri()))
      )
      // the extension
      nativeModel.add(
        reifiedAnnotationUri,
        nativeModel.createProperty(DomainExtensionModel.Extension.value.iri()),
        nextAnnotationValue
      )

      // the link back to the annotation definition
      val annotationLink = nativeModel.createResource(reifiedAnnotationUri.getURI + "_link")
      nativeModel.add(
        annotationLink,
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
      )
      nativeModel.add(
        annotationLink,
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource(typeMapping(CustomDomainPropertyModel.`type`.head.iri()))
      )
      nativeModel.add(
        annotationLink,
        nativeModel.createProperty(CanonicalWebAPISpecDialectExporter.DesignLinkTargetField.value.iri()),
        nextAnnotation
      )

      // We try to also add the name of the annotation to the annotation link
      val maybeAnnotationNameIt = nativeModel.listObjectsOfProperty(
        nextAnnotation.asResource(),
        nativeModel.createProperty(CustomDomainPropertyModel.Name.value.iri())
      )
      if (maybeAnnotationNameIt.hasNext) {
        nativeModel.add(
          annotationLink,
          nativeModel.createProperty(CustomDomainPropertyModel.Name.value.iri()),
          maybeAnnotationNameIt.next().asLiteral()
        )
      }

      // we get definedBy to the linked annotation
      nativeModel.add(
        reifiedAnnotationUri,
        nativeModel.createProperty(DomainExtensionModel.DefinedBy.value.iri()),
        annotationLink
      )

    }

  }

  protected def defaultIri(metadata: Obj): String = metadata.`type`.head.iri()

  /**
    * Transforms a WebAPI model parsed by AMF from a RAML/OAS document into a canonical WebAPI model compatible with the canonical WebAPI AML dialect
    */
  def transform(unit: BaseUnit): Future[BaseUnit] = unit match {
    case _: Document => Future.successful(cleanAMFModel(unit))
    case _           => throw DocumentExpectedException("Expected Document for CanonicalWebAPISpecTransformation")
  }
}

case class DocumentExpectedException(message: String) extends RuntimeException(message)
