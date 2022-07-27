package amf.cache

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.{AMFConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.platform.resource.ResourceNotFound
import amf.core.client.scala.config.{CachedReference, UnitCache}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, Module}
import amf.core.client.scala.validation.AMFValidationReport
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class UnitCacheTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Without resolve - Simple API") {
    val path        = "file://amf-cli/shared/src/test/resources/cache/api-library/"
    val libraryPath = path + "library.raml"
    val mainPath    = path + "api.raml"

    runCacheTest(mainPath, Seq(libraryPath), shouldResolve = false) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
      assert(root.references.head.isInstanceOf[Module])
    }
  }

  test("Without resolve - Multiple References Mixed") {
    val path        = "file://amf-cli/shared/src/test/resources/cache/api-multiple-references/"
    val libraryPath = path + "library.raml"
    val type1Path   = path + "datatypeC.raml"
    val mainPath    = path + "api.raml"

    runCacheTest(mainPath, Seq(libraryPath, type1Path), shouldResolve = false) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
      assert(root.references.size == 3)
    }
  }

  test("Without resolve - rt with reference of root type declaration") {

    val path     = "file://amf-cli/shared/src/test/resources/cache/api-rt/"
    val rtPath   = path + "rt.raml"
    val mainPath = path + "api.raml"

    runCacheTest(mainPath, Seq(rtPath), shouldResolve = false) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
    }
  }

  test("Without resolve - trait with reference of root type declaration") {

    val path      = "file://amf-cli/shared/src/test/resources/cache/api-trait/"
    val traitPath = path + "trait.raml"
    val mainPath  = path + "api.raml"

    runCacheTest(mainPath, Seq(traitPath), shouldResolve = false) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
    }
  }

  test("Resolved - Library fragment with complex types") {

    val path     = "file://amf-cli/shared/src/test/resources/cache/api-complex-lib-1/"
    val libPath  = path + "library.raml"
    val mainPath = path + "api.raml"

    runCacheTest(mainPath, Seq(libPath), shouldResolve = true) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
      assert(
        root
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[Api]
          .endPoints
          .head
          .operations(1)
          .request
          .payloads
          .head
          .schema
          .asInstanceOf[NodeShape]
          .properties
          .size == 6
      )
    }
  }

  test("Resolved - Library fragment with complex rt") {

    val path     = "file://amf-cli/shared/src/test/resources/cache/api-complex-lib-2/"
    val libPath  = path + "library.raml"
    val mainPath = path + "api.raml"

    runCacheTest(mainPath, Seq(libPath), shouldResolve = false) { (root, report) =>
      assert(report.conforms)
      assert(root.references.nonEmpty)
      assert(
        root
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[Api]
          .endPoints
          .head
          .operations
          .head
          .request
          .payloads
          .head
          .schema
          .asInstanceOf[NodeShape]
          .properties
          .size == 2
      )
    }
  }

  test("Test API with repeated file names in different paths") {

    val path     = "file://amf-cli/shared/src/test/resources/cache/api-inner-directories/"
    val libPath  = path + "pseudo-exchange-modules/lib.raml"
    val typePath = path + "pseudo-exchange-modules/type.raml"
    val mainPath = path + "api.raml"

    runCacheTest(mainPath, Seq(libPath, typePath), shouldResolve = true) { (_, report) =>
      assert(report.conforms)
    }
  }

  test("Test cached library with included trait") {

    val path      = "file://amf-cli/shared/src/test/resources/cache/lib-with-trait/"
    val libPath   = path + "lib.raml"
    val traitPath = path + "trait.raml"
    val mainPath  = path + "api.raml"

    runCacheTest(mainPath, Seq(libPath, traitPath), shouldResolve = true) { (_, report) =>
      assert(report.conforms)
    }
  }

  test("Test cached library with spaces at name (encode)") {

    val path     = "file://amf-cli/shared/src/test/resources/cache/ref-with-spaces/"
    val refPath  = path + "name spaced.raml"
    val mainPath = path + "api.raml"

    runCacheTest(mainPath, Seq(refPath), shouldResolve = true) { (_, report) =>
      assert(report.conforms)
    }
  }

  test("Test multiple resolved types cached") {

    val path      = "file://amf-cli/shared/src/test/resources/cache/multiples-types-cached/"
    val type1Path = path + "types/Account.raml"
    val type2Path = path + "types/AccountOwner.raml"
    val mainPath  = path + "api.raml"

    runCacheTest(mainPath, Seq(type1Path, type2Path), shouldResolve = true) { (_, report) =>
      assert(report.conforms)
    }
  }

  test("Test multiple resolved types (1 cached 1 not)") {

    val path      = "file://amf-cli/shared/src/test/resources/cache/multiples-types-cached/"
    val type1Path = path + "types/Account.raml"
    val mainPath  = path + "api.raml"

    runCacheTest(mainPath, Seq(type1Path), shouldResolve = true) { (_, report) =>
      assert(report.conforms)
    }
  }

  private def createClientWithCache(filesToCache: Seq[String], shouldResolve: Boolean): Future[AMFConfiguration] = {
    val client = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler).baseUnitClient()
    val listOfFutures = filesToCache.map { file =>
      client
        .parse(file)
        .map(_.baseUnit)
        .map { unit =>
          if (shouldResolve)
            client.transform(unit, PipelineId.Cache).baseUnit
          else unit
        }
        .map { unit =>
          CachedReference(file, unit)
        }
    }
    Future
      .sequence(listOfFutures)
      .map(references => RAMLConfiguration.RAML10().withUnitCache(CustomUnitCache(references)))
  }

  private def runCacheTest(main: String, filesToCache: Seq[String], shouldResolve: Boolean)(
      assert: (BaseUnit, AMFValidationReport) => Assertion
  ) = {
    for {
      client <- createClientWithCache(filesToCache, shouldResolve).map(_.baseUnitClient())
      root   <- client.parseDocument(main).map(_.document)
      report <- client.validate(root)
    } yield {
      assert(root, report)
    }
  }
}

case class CustomUnitCache(references: Seq[CachedReference]) extends UnitCache {

  /** If the resource not exists, you should return a future failed with an ResourceNotFound exception. */
  override def fetch(url: String): Future[CachedReference] =
    references.find(r => r.url == url) match {
      case Some(value) =>
        Future.successful { value }
      case _ =>
        Future.failed[CachedReference](new ResourceNotFound("Reference not found"))
    }
}
