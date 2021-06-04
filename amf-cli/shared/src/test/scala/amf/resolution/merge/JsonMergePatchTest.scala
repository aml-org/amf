package amf.resolution.merge

import amf.client.convert.BaseUnitConverter
import amf.client.parse.DefaultErrorHandler
import amf.client.remod.ParseConfiguration
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.SpecOrdering
import amf.core.model.document.Document
import amf.core.model.domain.{DataNode, ScalarNode}
import amf.core.parser.{ParserContext, YMapOps}
import amf.core.remote.Vendor.AMF
import amf.emit.AMFRenderer
import amf.facades.Validation
import amf.io.{FileAssertionTest, MultiJsonldAsyncFunSuite}
import amf.plugins.document.apicontract.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.async.Subscribe
import amf.plugins.document.apicontract.parser.spec.async.parser.{AsyncMessageParser, AsyncOperationParser}
import amf.plugins.document.apicontract.parser.spec.common.DataNodeParser
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.domain.shapes.resolution.stages.merge.{AsyncJsonMergePatch, AsyncKeyCriteria, JsonMergePatch}
import amf.plugins.domain.apicontract.models.{Message, Operation}
import org.mulesoft.common.io.Fs
import org.scalatest.{Assertion, Matchers}
import org.yaml.model.{YDocument, YMap, YNode}
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.concurrent.{ExecutionContext, Future}

class JsonMergePatchTest extends MultiJsonldAsyncFunSuite with Matchers with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath                                             = "amf-cli/shared/src/test/resources/resolution/merge"
  val operationBuilder: OperationDocumentHandler           = OperationDocumentHandler()
  val messageBuilder: MessageDocumentHandler               = MessageDocumentHandler()
  val dataNodeBuilder: DataNodeDocumentHandler             = DataNodeDocumentHandler()

  class Fixture(val testName: String,
                basePath: String,
                dirName: String,
                val handler: DocumentHandler,
                targetName: String,
                patchName: String,
                goldenName: String) {

    def golden: String = s"$basePath/$dirName/$goldenName"
    def target: String = s"$basePath/$dirName/$targetName"
    def patch: String  = s"$basePath/$dirName/$patchName"
  }

  case class JsonLdGoldenFixture(override val testName: String,
                                 dirName: String,
                                 override val handler: DocumentHandler,
                                 goldenName: String = "golden.%s")
      extends Fixture(testName, basePath, dirName, handler, "target.yaml", "patch.yaml", goldenName) {}

  case class YamlGoldenFixture(override val testName: String, dirName: String, override val handler: DocumentHandler)
      extends Fixture(testName, basePath, dirName, handler, "target.yaml", "patch.yaml", "golden.yaml")

  fixtures.foreach {
    case _ @JsonLdGoldenFixture(testName, dirName, handler, goldenName) =>
      multiGoldenTest(s"JsonMergePatch - $testName", goldenName) { config =>
        // TODO migrate to render options converter
        val renderOptions = if (config.renderOptions.isFlattenedJsonLd) {
          handler.defaultRenderOptions.withFlattenedJsonLd
        } else {
          handler.defaultRenderOptions.withoutFlattenedJsonLd
        }

        run(JsonLdGoldenFixture(testName, dirName, handler, config.golden), Some(renderOptions))
      }
    case f: Fixture =>
      test(s"JsonMergePatch - ${f.testName}") {
        run(f)
      }
  }

  def fixtures: Seq[Fixture] = Seq(
    JsonLdGoldenFixture("Simple operation", "operation-simple", operationBuilder),
    JsonLdGoldenFixture("Operation with tags", "operation-tags", operationBuilder),
    JsonLdGoldenFixture("Operation with documentation", "operation-documentation", operationBuilder),
    JsonLdGoldenFixture("Operation with same binding", "operation-same-binding", operationBuilder),
    JsonLdGoldenFixture("Operation with different bindings", "operation-different-binding", operationBuilder),
    JsonLdGoldenFixture("Operation with bindings with nested schemas",
                        "operation-binding-nested-schema",
                        operationBuilder),
    JsonLdGoldenFixture("Simple message", "message-simple", messageBuilder),
    JsonLdGoldenFixture("Message with different examples", "message-different-examples", messageBuilder),
    JsonLdGoldenFixture("Message with same examples", "message-same-example-names", messageBuilder),
    JsonLdGoldenFixture("Message with tags", "message-tags", messageBuilder),
    JsonLdGoldenFixture("Message with documentation", "message-documentation", messageBuilder),
    JsonLdGoldenFixture("Message with same binding", "message-same-binding", messageBuilder),
    JsonLdGoldenFixture("Message with different bindings", "message-different-binding", messageBuilder),
    YamlGoldenFixture("Rfc example 1", "rfc-example-1", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 2", "rfc-example-2", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 3", "rfc-example-3", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 4", "rfc-example-4", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 5", "rfc-example-5", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 6", "rfc-example-6", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 7", "rfc-example-7", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 8", "rfc-example-8", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 9", "rfc-example-9", dataNodeBuilder),
    YamlGoldenFixture("Rfc example 13", "rfc-example-13", dataNodeBuilder),
    YamlGoldenFixture("Rfc example last", "rfc-example-last", dataNodeBuilder)
  )

  def run(fixture: Fixture, renderOptions: Option[RenderOptions] = None): Future[Assertion] = {
    val golden   = fixture.golden
    val document = fixture.handler.build(fixture.target, fixture.patch)
    for {
      _ <- Validation(platform)
      f <- Future.successful {
        renderOptions match {
          case Some(ro) => fixture.handler.renderToString(document, ro)
          case _        => fixture.handler.renderToString(document)
        }
      }
      file   <- writeTemporaryFile(golden)(f)
      result <- assertDifferences(file, golden)
    } yield result
  }

  trait DocumentHandler extends BaseUnitConverter {

    def build(targetFile: String, patchFile: String): Document

    def getMerger: JsonMergePatch = AsyncJsonMergePatch()

    def getBogusParserCtx: AsyncWebApiContext =
      new Async20WebApiContext("loc", Seq(), ParserContext(config = ParseConfiguration(DefaultErrorHandler())))

    def renderToString(document: Document, renderOptions: RenderOptions = defaultRenderOptions): String =
      new AMFRenderer(document, AMF, renderOptions, None).renderToString

    def defaultRenderOptions: RenderOptions = new RenderOptions().withPrettyPrint
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
        .map(entry => AsyncOperationParser(entry, (o: Operation) => o.withId(id))(getBogusParserCtx).parse())
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
        .map(entry => AsyncMessageParser(YMapEntryLike(entry), id, Option(Subscribe))(getBogusParserCtx).parse())
        .get
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
      val content  = platform.fs.syncFile(filePath).read()
      val document = YamlParser(content).documents().head
      document
        .as[YMap]
        .key("node")
        .map(entry => DataNodeParser(entry.value)(WebApiShapeParserContextAdapter(getBogusParserCtx)).parse())
        .get
    }

    override def renderToString(document: Document, renderOptions: RenderOptions): String = {
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
      YamlRender.render(ydocument)
    }

    override def getMerger: JsonMergePatch =
      JsonMergePatch({
        case scalar: ScalarNode => scalar.value.value() == "null"
        case _                  => false
      }, AsyncKeyCriteria())
  }
}
