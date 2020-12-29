package amf

import amf.client.parse.DefaultParserErrorHandler
import amf.core.parser.ParserContext
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaAstIndex
import org.scalatest.{AsyncFunSuite, Matchers}
import org.yaml.parser.YamlParser

class MyPocTest extends AsyncFunSuite with PlatformSecrets {

  test("Possible shapes being parsed") {
    implicit val ctx  = getBogusParserCtx
    val path          = "/Users/tfernandez/mulesoft/amf/amf-client/shared/src/test/resources/POC/something.yaml"
    val content       = platform.fs.syncFile(path).read()
    val node          = YamlParser(content, path).document().node
    val index         = JsonSchemaAstIndex(node)
    val possiblesKeys = Set("$schema", "type")
    val entries = index.keySet
      .filter(x => possiblesKeys.contains(getLastSegmentOfUri(x)))
      .map(getHeadOfUri)
      .flatMap(index.getNodeAndEntry)
    println(entries.size)
    succeed
  }

  private def getLastSegmentOfUri(uri: String) = uri.split("/").lastOption.getOrElse("")
  private def getHeadOfUri(uri: String) = {
    val uri2 = uri.split("/").dropRight(1).mkString("/")
    if (uri2.isEmpty) "/" else uri2
  }

  private def getBogusParserCtx: AsyncWebApiContext =
    new Async20WebApiContext("loc", Seq(), ParserContext(eh = UnhandledParserErrorHandler))
}
