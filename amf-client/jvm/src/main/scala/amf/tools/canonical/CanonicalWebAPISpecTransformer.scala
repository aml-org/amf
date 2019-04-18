package amf.tools.canonical

import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.rdf.RdfModelParser
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.metamodel.domain.NodeMappingModel
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import org.apache.jena.rdf.model.{Model, Statement}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CanonicalWebAPISpecTransformer  extends PlatformSecrets {

  val CANONICAL_WEBAPI_NAME = "WebAPI Spec 1.0"

  // Properties that will be inserted in the graph
  val REPO_INTERNAL_REF = "http://anypoint.com/vocabs/digital-repository#internalReference"
  val REPO_ASSET_LOCATION = "http://anypoint.com/vocabs/digital-repository#location"
  val REPO_LINK_TARGET = "http://anypoint.com/vocabs/digital-repository#link-target"
  val REPO_LINK_LABEL = "http://anypoint.com/vocabs/digital-repository#link-label"
  val REPO_EXTENDS = "http://anypoint.com/vocabs/digital-repository#extends"

  /**
    * Cleans plugins that will cause a conflict with the canonical WebAPI dialect revivers
    * @return
    */
  protected def removeWebAPIPlugin() = Future {
    AMFPluginsRegistry.unregisterDomainPlugin(WebAPIDomainPlugin)
    AMFPluginsRegistry.unregisterDomainPlugin(DataShapesDomainPlugin)
  }

  /**
    * Resets the WebAPI plugins
    * @return
    */
  protected def registerWebAPIPlugin() = Future {
    AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
    AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)
  }

  protected def findWebAPIDialect: Option[Dialect] =
    AMLPlugin.registry.allDialects().find(_.nameAndVersion() == CANONICAL_WEBAPI_NAME)

  /**
    * Creates a map from the node mapping in the canonical web api dialect to the mapped
    * class in the WebAPI vocabulary
    * @return
    */
  protected def buildCanonicalClassMapping: Map[String, String] = {
    findWebAPIDialect match {
      case Some(webApiDialect) =>
        val nodeMappings = webApiDialect.declares.collect { case mapping: NodeMapping => mapping }
        nodeMappings.foldLeft(Map[String,String]()) { case (acc, mapping) =>
          acc + (mapping.nodetypeMapping.value() -> mapping.id)
        }
      case None                =>
        throw new Exception("Cannot find WebAPI 1.0 Dialect in Dialect registry")
    }
  }

  /**
    * Renames the URI of a resource node in the RDF graph: incoming and outgoing edges
    */
  def renameResource(source: String, target: String, nativeModel: Model): Unit = {
    // remove matching S(source)-p-o statements, add S(target)-p-o statements
    val subjectsIterator = nativeModel.listStatements(nativeModel.createResource(source), null, null)
    val acc = mutable.ArrayBuffer[Statement]()
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

  /**
    * Renames the URI of a resource node in the RDF graph: incoming and outgoing edges
    */
  def renameProperty(source: String, target: String, nativeModel: Model): Unit = {
    // remove matching s-p(source)-o statements, add s-p(target)-o statements
    val propertiesIterator = nativeModel.listStatements(null, nativeModel.createProperty(source), null)
    val acc = mutable.ArrayBuffer[Statement]()
    while (propertiesIterator.hasNext) {
      val nextStatement = propertiesIterator.nextStatement()
      acc += nextStatement
    }
    acc.foreach { st =>
      nativeModel.remove(st)
      val updatedStatement = nativeModel.createStatement(
        st.getSubject,
        nativeModel.createProperty(target),
        st.getObject
      )
      nativeModel.add(updatedStatement)
    }
  }

  def mapBaseUnits(unit: String, dialect:Dialect, nativeModel: Model) = {
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

    val mappedDocumentType = if (allTypes.contains((Namespace.Document + "Document").iri())) {
      (Namespace.ApiContract + "WebAPIDocument").iri()
    } else if (allTypes.contains((Namespace.Document + "Module").iri())) {
      (Namespace.ApiContract + "LibraryModule").iri()
    } else if (allTypes.contains((Namespace.Document + "ExternalFragment").iri())) {
      (Namespace.Document + "ExternalFragment").iri()
    } else {
      val cleanTypes = allTypes.filter { t =>
        t != (Namespace.Document + "Unit").iri() &&
        t != (Namespace.Document + "Document").iri() &&
        t != (Namespace.Document + "Fragment").iri()
      }
      Namespace.ApiContract.base + cleanTypes.head.split("#").last
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
      case _                 =>
        println(s"Couldn't find node mapping for ${mappedDocumentType}")
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


    println(s"Generated TYPE: $mappedDocumentType")


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
      case _                    => // ignore
    }
  }
  /**
    * Transforms the doc:Units in the graph into the domain elements for assets in the canonical web api spec dialect
    */
  def preProcessUnits(nativeModel: Model): Unit = {
    findWebAPIDialect match {
      // we need the dialect ID in this particular instance of AMF
      case Some(dialect) =>

        // we save the top level document we will not transform into a domain level asset
        val topLevelUnit = nativeModel.listSubjectsWithProperty(
          nativeModel.createProperty((Namespace.Rdf + "type").iri()),
          nativeModel.createResource((Namespace.Document + "Document").iri())
        ).next().getURI

        // get all the document units
        val unitsIterator = nativeModel.listSubjectsWithProperty(
          nativeModel.createProperty((Namespace.Rdf + "type").iri()),
          nativeModel.createResource((Namespace.Document + "Unit").iri())
        )
        val unitUris = mutable.ArrayBuffer[String]()
        while(unitsIterator.hasNext) {
          unitUris += unitsIterator.nextResource().getURI
        }

        // for each document unit that is not the root one, we transform that into the canonical webpi spec asset fragment node
        // defined in the SpecDocument node mapping schema
        unitUris.foreach { unit =>

          val unitResource = nativeModel.createResource(unit)

          // Let's manipulate the @type of the unit to match the dialect expectations
          mapBaseUnits(unit, dialect, nativeModel)


          // relations: normal doc:references to the domain level internalReference
          val allReferences = mutable.ArrayBuffer[String]()
          val referencesIterator = nativeModel.listObjectsOfProperty(
            nativeModel.createResource(unit),
            nativeModel.createProperty((Namespace.Document + "references").iri()),
          )
          while (referencesIterator.hasNext) {
            allReferences += referencesIterator.next().asResource().getURI
          }
          allReferences.foreach { ref =>
            nativeModel.remove(
              nativeModel.createResource(unit),
              nativeModel.createProperty((Namespace.Document + "references").iri()),
              nativeModel.createResource(ref)
            )
            nativeModel.add(
              nativeModel.createResource(unit),
              nativeModel.createProperty(REPO_INTERNAL_REF),
              nativeModel.createResource(ref)
            )
          }
        }

        // we introduce a new top level document with the URI of the old top level document
        // we rename the old top level document (now a domain element)
        val encodedElement = topLevelUnit + "#/rootAsset" // picking this URI is safe, not present in the model
        // rename so twe can reuse the old main root URI
        renameResource(topLevelUnit, encodedElement, nativeModel)
        val topLevel = nativeModel.createResource(topLevelUnit)
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

      case None          => //ignore
    }

    renameProperty((Namespace.Document + "link-target").iri(), REPO_LINK_TARGET, nativeModel)
    renameProperty((Namespace.Document + "link-label").iri(), REPO_LINK_LABEL, nativeModel)
    renameProperty((Namespace.Document + "extends").iri(), REPO_EXTENDS, nativeModel)
  }

  /**
    * Cleans the input WebAPI model adding the required information to the
    * underlying RDF graph and uses it to build the canonical WebAPI dialect
    * instance as output
    * @param unit a AMF WebAPI model parsed from RAML / OAS
    * @return Equivalent Canonical WebAPI AML dialect instance
    */
  protected def cleanAMFModel(unit: BaseUnit): BaseUnit = {
    val mapping = buildCanonicalClassMapping
    val model = unit.toNativeRdfModel()
    val nativeModel = model.native().asInstanceOf[Model]

    preProcessUnits(nativeModel)

    // First update document to DialectInstance document
    val doc = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "Document").iri())
    ).next().getURI
    nativeModel.add(
      nativeModel.createResource(doc),
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Meta + "DialectInstance").iri())
    )
    findWebAPIDialect match {
      case Some(dialect) =>
        nativeModel.add(
          nativeModel.createResource(doc),
          nativeModel.createProperty((Namespace.Meta + "definedBy").iri()),
          nativeModel.createResource(dialect.id)
        )
      case None          => //ignore
    }

    // Find all domain elements
    var it = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Document + "DomainElement").iri())
    )
    val domainElements: mutable.ListBuffer[String] = mutable.ListBuffer()
    while (it.hasNext()) {
      domainElements += it.next().getURI
    }

    // Find all shapes
    it = nativeModel.listSubjectsWithProperty(
      nativeModel.createProperty((Namespace.Rdf + "type").iri()),
      nativeModel.createResource((Namespace.Shapes + "Shape").iri())
    )
    while (it.hasNext()) {
      domainElements += it.next().getURI
    }

    // Find the type of all the domain elements and map it to a node in the canonical dialect
    val domainElementsMapping = mutable.Map[String, String]()
    domainElements.foreach { domainElement =>
      val nodeIt = nativeModel.listObjectsOfProperty(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty((Namespace.Rdf + "type").iri())
      )


      // We need to deal with node shape inheritance
      // These flags allow us to track if we found anyshape or shape in case
      // we cannot find a more specific shape
      var foundShape: Option[String] = None
      var foundAnyShape:Option[String] = None
      var found = false
      while (nodeIt.hasNext) {
        val nextType = nodeIt.next().asResource().getURI
        mapping.get(nextType) match {
          case Some(dialectNode) =>
            // dealing with inheritance here
            if (! dialectNode.endsWith("#/declarations/Shape") && !dialectNode.endsWith("#/declarations/AnyShape")) {
              found = true
              domainElementsMapping += (domainElement -> dialectNode)
            } else if (dialectNode.endsWith("#/declarations/Shape")) {
              foundShape = Some(dialectNode)
            } else if (dialectNode.endsWith("#/declarations/AnyShape")) {
              foundAnyShape = Some(dialectNode)
            }
          case _                 => // ignore
        }
      }

      // Set the base shape node if we have find it and we didn't find anything more specific
      if (!found && foundAnyShape.isDefined) {
        domainElementsMapping += (domainElement -> foundAnyShape.get)
        found = true
      }
      if (!found && foundShape.isDefined) {
        domainElementsMapping += (domainElement -> foundShape.get)
        found = true
      }
    }

    // Add the dialect domain element and right mapped node mapping type in the model
    domainElementsMapping.foreach { case (domainElement, nodeMapping) =>
      nativeModel.add(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource((Namespace.Meta + "DialectDomainElement").iri())
      )
      nativeModel.add(
        nativeModel.createResource(domainElement),
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource(nodeMapping)
      )
    }

    new RdfModelParser(platform)(ParserContext()).parse(model, unit.id)
  }

  /**
    * Transforms a WebAPI model parsed by AMF from a RAML/OAS document into
    * a canonical WebAPI model compatible with the canonical WebAPI AML dialect
    * @param unit
    * @return
    */
  def transform(unit: BaseUnit): Future[BaseUnit] = {
    for {
      _           <- removeWebAPIPlugin()
      transformed <- Future { cleanAMFModel(unit) }
      _           <- registerWebAPIPlugin()
    } yield {
      transformed
    }
  }
}
