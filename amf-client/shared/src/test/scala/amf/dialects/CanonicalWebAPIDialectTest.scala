package amf.dialects
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.rdf.RdfModelParser
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{AmfJsonHint, Syntax, Vendor, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import org.apache.jena.rdf.model.Model
import org.scalatest.AsyncFunSuite

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class CanonicalWebAPIDialectTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def removeWebAPIPlugin() = Future {
    AMFPluginsRegistry.unregisterDomainPlugin(WebAPIDomainPlugin)
    AMFPluginsRegistry.unregisterDomainPlugin(DataShapesDomainPlugin)
  }

  def findWebAPIDialect =
    AMLPlugin.registry.allDialects().find(_.nameAndVersion() == "WebAPI 1.0")

  def buildCanonicalClassMapping: Map[String, String] = {
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

  def cleanAMFModel(unit: BaseUnit): BaseUnit = {
    val mapping = buildCanonicalClassMapping
    val model = unit.toNativeRdfModel()
    println("MODEL:")
    println(model.toN3())
    val nativeModel = model.native().asInstanceOf[Model]

    // First update document to DialectInstance document
    val doc = nativeModel.listSubjectsWithProperty(nativeModel.createProperty((Namespace.Document + "encodes").iri())).next().getURI
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

      while (nodeIt.hasNext) {
        val nextType = nodeIt.next().asResource().getURI
        mapping.get(nextType) match {
          case Some(dialectNode) => domainElementsMapping += (domainElement -> dialectNode)
          case _                 => // ignore
        }
      }
    }

    // Add the dialect domain element and right mapped node mapping type in the model
    domainElementsMapping.foreach { case (domainElement, nodeMapping) =>
      println(s"  *  TRANSFORMING $domainElement INTO A DIALECT DOMAIN ELEMENT")
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

  test("HERE_HERE Test parsed RAML/OAS WebAPIs can be re-parsed with the WebAPI dialect") {
    for {
      _               <- AMF.init()
      _               <- Future(amf.Core.registerPlugin(AMLPlugin))
      v               <- Validation(platform).map(_.withEnabledValidation(false))
      _               <- {
        println(s"  ===> 1")
        AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies2/production/canonical_webapi.yaml", platform, VocabularyYamlHint, v).build()
      }
      unit            <- {
        println(s"  ===> 2")
        AMFCompiler("file://amf-client/shared/src/test/resources/upanddown/banking-api.raml.jsonld", platform, AmfJsonHint, v).build()
      }
      _               <- removeWebAPIPlugin()
      dialectInstance <- {
        println(s"  ===> 3")
        Future { cleanAMFModel(unit)}
      }
      rendered          <- {
        println(s"  ===> 4")
        new AMFRenderer(dialectInstance, Vendor.AML, RenderOptions(), Some(Syntax.Yaml)).renderToString
      }
    } yield {
      println(dialectInstance)
      println("RENDERED")
      println(rendered)
      println(unit.toNativeRdfModel())
      println(unit.toNativeRdfModel().toN3())
      assert(true)
    }
  }
}
