package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.unsafe.PlatformSecrets
import amf.graphql.client.scala.GraphQLConfiguration
import org.mulesoft.common.io.FileSystem
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.Future

class GraphQLTCKValidationTest extends AsyncFunSuite with PlatformSecrets {
  val tckPath: String  = "amf-cli/shared/src/test/resources/graphql/tck"
  val apisPath: String = s"$tckPath/apis"

  val fs: FileSystem = platform.fs

  // Test valid APIs
  fs.syncFile(s"$apisPath/valid").list.foreach { api =>
    ignore(s"GraphQL TCK > Apis > Valid > $api: should conform") { assertConforms(s"$apisPath/valid/$api") }
  }

  // Test invalid APIs
  fs.syncFile(s"$apisPath/invalid").list.foreach { api =>
    ignore(s"GraphQL TCK > Apis > Invalid > $api: should not conform") { assertNotConforms(s"$apisPath/invalid/$api") }
  }

  def assertConforms(api: String): Future[Assertion] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    for {
      parsing        <- client.parse(s"file://$api")
      transformation <- Future.successful(client.transform(parsing.baseUnit))
      validation     <- client.validate(transformation.baseUnit)
    } yield {
      assert(parsing.conforms && transformation.conforms && validation.conforms)
    }
  }

  def assertNotConforms(api: String): Future[Assertion] = {
    val client = GraphQLConfiguration.GraphQL().baseUnitClient()
    for {
      parsing        <- client.parse(s"file://$api")
      transformation <- Future.successful(client.transform(parsing.baseUnit))
      validation     <- client.validate(transformation.baseUnit)
    } yield {
      assert(!parsing.conforms || !transformation.conforms || !validation.conforms)
    }
  }

}
