package amf.tools.canonical
import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.rdf.RdfModelParser
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import org.apache.jena.rdf.model.Model

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CanonicalWebAPITransformer extends PlatformSecrets {

  val CANONICAL_WEBAPI_NAME = "WebAPI 1.0"

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
        nodeMappings.foldLeft(Map[String, String]()) {
          case (acc, mapping) =>
            acc + (mapping.nodetypeMapping.value() -> mapping.id)
        }
      case None =>
        throw new Exception("Cannot find WebAPI 1.0 Dialect in Dialect registry")
    }
  }

  /**
    * Cleans the input WebAPI model adding the required information to the
    * underlying RDF graph and uses it to build the canonical WebAPI dialect
    * instance as output
    * @param unit a AMF WebAPI model parsed from RAML / OAS
    * @return Equivalent Canonical WebAPI AML dialect instance
    */
  protected def cleanAMFModel(unit: BaseUnit): BaseUnit = {
    val mapping     = buildCanonicalClassMapping
    val model       = unit.toNativeRdfModel()
    val nativeModel = model.native().asInstanceOf[Model]

    // First update document to DialectInstance document
    val doc = nativeModel
      .listSubjectsWithProperty(
        nativeModel.createProperty((Namespace.Rdf + "type").iri()),
        nativeModel.createResource((Namespace.Document + "Document").iri())
      )
      .next()
      .getURI
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
      case None => //ignore
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
      var foundShape: Option[String]    = None
      var foundAnyShape: Option[String] = None
      var found                         = false
      while (nodeIt.hasNext) {
        val nextType = nodeIt.next().asResource().getURI
        mapping.get(nextType) match {
          case Some(dialectNode) =>
            // dealing with inheritance here
            if (!dialectNode.endsWith("#/declarations/Shape") && !dialectNode.endsWith("#/declarations/AnyShape")) {
              found = true
              domainElementsMapping += (domainElement -> dialectNode)
            } else if (dialectNode.endsWith("#/declarations/Shape")) {
              foundShape = Some(dialectNode)
            } else if (dialectNode.endsWith("#/declarations/AnyShape")) {
              foundAnyShape = Some(dialectNode)
            }
          case _ => // ignore
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
    domainElementsMapping.foreach {
      case (domainElement, nodeMapping) =>
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

    new RdfModelParser(platform)(ParserContext(eh = UnhandledParserErrorHandler)).parse(model, unit.id)
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
