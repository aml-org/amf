package amf.resolution.merge

import amf.Core
import amf.client.convert.{BaseUnitConverter, NativeOps}
import amf.client.parse.DefaultParserErrorHandler
import amf.client.render.{AmfGraphRenderer, RenderOptions}
import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.SpecOrdering
import amf.core.model.document.Document
import amf.core.model.domain.{DataNode, ScalarNode}
import amf.core.parser.{ParserContext, YMapOps}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.parser.spec.async.Subscribe
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.document.webapi.parser.spec.domain.{AsyncMessageParser, AsyncOperationParser}
import amf.plugins.domain.shapes.resolution.stages.merge.{AsyncKeyCriteria, JsonMergePatch}
import amf.plugins.domain.webapi.models.{Message, Operation}
import org.mulesoft.common.io.Fs
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import org.yaml.model.{YDocument, YMap, YNode}
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.concurrent.Future

trait JsonMergePatchTest
    extends AsyncFunSuite
    with Matchers
    with FileAssertionTest
    with BaseUnitConverter
    with NativeOps {

  val basePath                                   = "amf-client/shared/src/test/resources/resolution/merge"
  val operationBuilder: OperationDocumentHandler = OperationDocumentHandler()
  val messageBuilder: MessageDocumentHandler     = MessageDocumentHandler()
  val dataNodeBuilder: DataNodeDocumentHandler   = DataNodeDocumentHandler()

  case class Fixture(name: String, folder: String, handler: DocumentHandler)

  fixtures.foreach { f =>
    test(s"JsonMergePatch - ${f.name}") {
      run(f)
    }
  }

  def fixtures = Seq(
    Fixture("Simple operation", "operation-simple", operationBuilder),
    Fixture("Operation with tags", "operation-tags", operationBuilder),
    Fixture("Operation with documentation", "operation-documentation", operationBuilder),
    Fixture("Operation with same binding", "operation-same-binding", operationBuilder),
    Fixture("Operation with different bindings", "operation-different-binding", operationBuilder),
    Fixture("Operation with bindings with nested schemas", "operation-binding-nested-schema", operationBuilder),
    Fixture("Simple message", "message-simple", messageBuilder),
    Fixture("Message with different examples", "message-different-examples", messageBuilder),
    Fixture("Message with same examples", "message-same-example-names", messageBuilder),
    Fixture("Message with tags", "message-tags", messageBuilder),
    Fixture("Message with documentation", "message-documentation", messageBuilder),
    Fixture("Message with same binding", "message-same-binding", messageBuilder),
    Fixture("Message with different bindings", "message-different-binding", messageBuilder),
    Fixture("Rfc example 1", "rfc-example-1", dataNodeBuilder),
    Fixture("Rfc example 2", "rfc-example-2", dataNodeBuilder),
    Fixture("Rfc example 3", "rfc-example-3", dataNodeBuilder),
    Fixture("Rfc example 4", "rfc-example-4", dataNodeBuilder),
    Fixture("Rfc example 5", "rfc-example-5", dataNodeBuilder),
    Fixture("Rfc example 6", "rfc-example-6", dataNodeBuilder),
    Fixture("Rfc example 7", "rfc-example-7", dataNodeBuilder),
    Fixture("Rfc example 8", "rfc-example-8", dataNodeBuilder),
    Fixture("Rfc example 9", "rfc-example-9", dataNodeBuilder),
    Fixture("Rfc example 13", "rfc-example-13", dataNodeBuilder),
    Fixture("Rfc example last", "rfc-example-last", dataNodeBuilder)
  )

  def run(fixture: Fixture): Future[Assertion] = {
    val golden   = fixture.handler.getGolden(basePath, fixture.folder)
    val document = fixture.handler.build(getTarget(fixture.folder), getPatch(fixture.folder))
    for {
      _      <- Core.init().asFuture
      f      <- fixture.handler.renderToString(document)
      file   <- writeTemporaryFile(golden)(f)
      result <- assertDifferences(file, golden)
    } yield result
  }

  def getTarget(folder: String) = basePath + "/" + folder + "/target.yaml"
  def getPatch(folder: String)  = basePath + "/" + folder + "/patch.yaml"
  def getGolden(folder: String) = basePath + "/" + folder + "/golden.jsonld"

  trait DocumentHandler extends BaseUnitConverter {

    def build(targetFile: String, patchFile: String): Document
    def getMerger: JsonMergePatch = JsonMergePatch(_ => false, AsyncKeyCriteria())
    def getBogusParserCtx: AsyncWebApiContext =
      new Async20WebApiContext("loc", Seq(), ParserContext(eh = DefaultParserErrorHandler()))
    def getGolden(basePath: String, folder: String) = basePath + "/" + folder + "/golden.jsonld"

    def renderToString(document: Document): Future[String] =
      new AmfGraphRenderer()
        .generateString(BaseUnitMatcher.asClient(document), new RenderOptions().withPrettyPrint)
        .asFuture
  }

  case class OperationDocumentHandler() extends DocumentHandler {

    override def build(targetFile: String, patchFile: String): Document = {
      val target = parseOperation(targetFile, "target")
      val patch  = parseOperation(patchFile, "patch")
      val merged = getMerger.merge(target, patch)
      Document().withId("testId").withEncodes(merged.asInstanceOf[Operation])
    }

    def parseOperation(filePath: String, id: String): Operation = {
      val content  = Fs.syncFile(filePath).read()
      val document = YamlParser(content).documents().head
      document
        .as[YMap]
        .key("subscribe")
        .map(entry =>
          AsyncOperationParser(entry, method => Operation().withId(id).withMethod(method))(getBogusParserCtx).parse())
        .get
    }
  }

  case class MessageDocumentHandler() extends DocumentHandler {
    override def build(targetFile: String, patchFile: String): Document = {
      val target = parseMessage(targetFile, "target")
      val patch  = parseMessage(patchFile, "patch")
      val merged = getMerger.merge(target, patch)
      Document().withId("testId").withEncodes(merged.asInstanceOf[Message])
    }

    def parseMessage(filePath: String, id: String): Message = {
      val content  = Fs.syncFile(filePath).read()
      val document = YamlParser(content).documents().head
      document
        .as[YMap]
        .key("message")
        .map(entry => AsyncMessageParser(id, entry.value.as[YMap], Subscribe)(getBogusParserCtx).parse())
        .get
        .head
    }
  }

  case class DataNodeDocumentHandler() extends DocumentHandler {
    override def build(targetFile: String, patchFile: String): Document = {
      val target = parseNode(targetFile, "target")
      val patch  = parseNode(patchFile, "patch")
      val merged = getMerger.merge(target, patch)
      Document().withId("testId").withEncodes(merged.asInstanceOf[DataNode])
    }

    def parseNode(filePath: String, id: String): DataNode = {
      val content  = Fs.syncFile(filePath).read()
      val document = YamlParser(content).documents().head
      document
        .as[YMap]
        .key("node")
        .map(entry => DataNodeParser(entry.value)(getBogusParserCtx).parse())
        .get
    }

    override def getGolden(basePath: String, folder: String) = basePath + "/" + folder + "/golden.yaml"

    override def renderToString(document: Document): Future[String] = {
      val emitters =
        DataNodeEmitter(document.encodes.asInstanceOf[DataNode], SpecOrdering.Default)(getBogusParserCtx.eh).emitters()
      val ydocument = YDocument {
        _.obj { b =>
          b.entry(
            YNode("node"),
            _.obj { pb =>
              traverse(emitters, pb)
            }
          )
        }
      }
      Future { YamlRender.render(ydocument) }
    }

    override def getMerger: JsonMergePatch =
      JsonMergePatch({
        case scalar: ScalarNode => scalar.value.value() == "null"
        case _                  => false
      }, AsyncKeyCriteria())
  }
}
