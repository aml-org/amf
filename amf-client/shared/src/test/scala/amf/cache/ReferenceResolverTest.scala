package amf.cache

import amf.RamlProfile
import amf.client.convert.CoreClientConverters._
import amf.client.convert.NativeOps
import amf.client.environment.DefaultEnvironment
import amf.client.model.document.{Document, Module}
import amf.client.model.domain.{NodeShape, WebApi}
import amf.client.parse.RamlParser
import amf.client.reference.ReferenceResolver
import amf.client.resolve.Raml10Resolver
import amf.client.resource.ResourceNotFound
import amf.client.{AMF, reference}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.internal.reference.{CachedReference => InternalCachedReference}
import amf.client.reference.CachedReference
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait ReferenceResolverTest extends AsyncFunSuite with Matchers with NativeOps {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  case class CustomReferenceResolver(references: Seq[CachedReference]) extends ReferenceResolver {

    /** If the resource not exists, you should return a future failed with an ResourceNotFound exception. */
    override def fetch(url: String): ClientFuture[reference.CachedReference] =
      references.find(r => r.url == url) match {
        case Some(value) =>
          Future { value._internal }.asClient
        case _ =>
          Future.failed[InternalCachedReference](new ResourceNotFound("Reference not found")).asClient
      }
  }

  test("Without resolve - Simple API") {

    val path        = "file://amf-client/shared/src/test/resources/cache/api-library/"
    val libraryPath = path + "library.raml"
    val mainPath    = path + "api.raml"

    for {
      _       <- AMF.init().asFuture
      library <- new RamlParser().parseFileAsync(libraryPath).asFuture
      environment <- {
        val references = Seq(new CachedReference(libraryPath, library, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
      assert(root.references().asSeq.head.isInstanceOf[Module])
    }
  }

  test("Without resolve - Multiple References Mixed") {

    val path        = "file://amf-client/shared/src/test/resources/cache/api-multiple-references/"
    val libraryPath = path + "library.raml"
    val type1Path   = path + "datatypeC.raml"
    val mainPath    = path + "api.raml"

    for {
      _         <- AMF.init().asFuture
      library   <- new RamlParser().parseFileAsync(libraryPath).asFuture
      datatype1 <- new RamlParser().parseFileAsync(type1Path).asFuture
      environment <- {
        val references = Seq(new CachedReference(libraryPath, library, resolved = false),
                             new CachedReference(type1Path, datatype1, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
      assert(root.references().asSeq.size == 3)
    }
  }

  test("Without resolve - rt with reference of root type declaration") {

    val path     = "file://amf-client/shared/src/test/resources/cache/api-rt/"
    val rtPath   = path + "rt.raml"
    val mainPath = path + "api.raml"

    for {
      _  <- AMF.init().asFuture
      rt <- new RamlParser().parseFileAsync(rtPath).asFuture
      environment <- {
        val references = Seq(new CachedReference(rtPath, rt, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
    }
  }

  test("Without resolve - trait with reference of root type declaration") {

    val path      = "file://amf-client/shared/src/test/resources/cache/api-trait/"
    val traitPath = path + "trait.raml"
    val mainPath  = path + "api.raml"

    for {
      _  <- AMF.init().asFuture
      tr <- new RamlParser().parseFileAsync(traitPath).asFuture
      environment <- {
        val references = Seq(new CachedReference(traitPath, tr, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
    }
  }

  test("Resolved - Library fragment with complex types") {

    val path     = "file://amf-client/shared/src/test/resources/cache/api-complex-lib-1/"
    val libPath  = path + "library.raml"
    val mainPath = path + "api.raml"

    for {
      _               <- AMF.init().asFuture
      library         <- new RamlParser().parseFileAsync(libPath).asFuture
      libraryResolved <- Future(new Raml10Resolver().resolve(library, ResolutionPipeline.EDITING_PIPELINE))
      environment <- {
        val references = Seq(new CachedReference(libPath, libraryResolved, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
      assert(
        root
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[WebApi]
          .endPoints
          .asSeq
          .head
          .operations
          .asSeq(1)
          .request
          .payloads
          .asSeq
          .head
          .schema
          .asInstanceOf[NodeShape]
          .properties
          .asSeq
          .size == 6)
    }
  }

  test("Resolved - Library fragment with complex rt") {

    val path     = "file://amf-client/shared/src/test/resources/cache/api-complex-lib-2/"
    val libPath  = path + "library.raml"
    val mainPath = path + "api.raml"

    for {
      _               <- AMF.init().asFuture
      library         <- new RamlParser().parseFileAsync(libPath).asFuture
      libraryResolved <- Future(new Raml10Resolver().resolve(library, ResolutionPipeline.EDITING_PIPELINE))
      environment <- {
        val references = Seq(new CachedReference(libPath, libraryResolved, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(report.conforms)
      assert(root.references().asSeq.nonEmpty)
      assert(
        root
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[WebApi]
          .endPoints
          .asSeq
          .head
          .operations
          .asSeq(1)
          .request
          .payloads
          .asSeq
          .head
          .schema
          .asInstanceOf[NodeShape]
          .properties
          .asSeq
          .size == 2)
    }
  }

  test("Test API with repeated file names in different paths") {

    val path     = "file://amf-client/shared/src/test/resources/cache/api-inner-directories/"
    val libPath  = path + "pseudo-exchange-modules/lib.raml"
    val typePath = path + "pseudo-exchange-modules/type.raml"
    val mainPath = path + "api.raml"

    for {
      _               <- AMF.init().asFuture
      library         <- new RamlParser().parseFileAsync(libPath).asFuture
      libraryResolved <- Future(new Raml10Resolver().resolve(library, ResolutionPipeline.EDITING_PIPELINE))
      dataType        <- new RamlParser().parseFileAsync(typePath).asFuture
      typeResolved    <- Future(new Raml10Resolver().resolve(dataType, ResolutionPipeline.EDITING_PIPELINE))
      environment <- {
        val references = Seq(new CachedReference(libPath, libraryResolved, resolved = false),
                             new CachedReference(typePath, typeResolved, resolved = false))
        Future.successful(
          DefaultEnvironment().withResolver(CustomReferenceResolver(references).asInstanceOf[ClientReference])
        )
      }
      root   <- new RamlParser(environment).parseFileAsync(mainPath).asFuture
      report <- AMF.validate(root, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      root
      assert(report.conforms)
    }
  }

}
