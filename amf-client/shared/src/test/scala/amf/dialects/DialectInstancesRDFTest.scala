package amf.dialects

import amf.client.model.document.Dialect
import amf.core.model.document.BaseUnit
import amf.core.rdf.RdfModel
import amf.core.remote.{Amf, Hint, Vendor, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class DialectInstancesRDFTest extends AsyncFunSuite with PlatformSecrets with BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/instances/"

  test("RDF 1 test") {
    withDialect("dialect1.raml", "example1.raml", "example1.ttl", VocabularyYamlHint, Amf)
  }




  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath) = {
    for {
      v         <- Validation(platform).map(_.withEnabledValidation(false))
      something <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res       <- cycleRdf(source, golden, hint, target)
    } yield {
      res
    }
  }

}
