package amf.shapes.internal.validation.plugin

import amf.core.client.common.validation.{ProfileName, ValidationMode}
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.annotations.SourceYPart
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.parser.LimitedParseConfig
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.plugins.render.JsonLDInstanceRenderHelper
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.context.JsonLdSchemaContext
import org.yaml.model.{YMap, YNode, YSequence}

import scala.concurrent.Future

object JsonSchemaBasedSpecValidationHelper {
  private lazy val config = ShapesConfiguration.predefined()

  def validateInstanceSync(
      instanceUnit: JsonLDInstanceDocument,
      schema: Shape,
      profile: ProfileName
  ): AMFValidationReport = {
    val encoded = instanceUnit.encodes.head.asInstanceOf[JsonLDObject]
    val content = JsonLDInstanceRenderHelper.renderToJson(encoded)
    val results = config
      .elementClient()
      .payloadValidatorFor(schema, Mimes.`application/json`, ValidationMode.StrictValidationMode)
      .syncValidate(content)
      .results
      .distinct
    val report = AMFValidationReport(getLocation(instanceUnit), profile, results)
    report
  }

  def validateInstance(
      instanceUnit: JsonLDInstanceDocument,
      schema: Shape,
      profile: ProfileName
  ): Future[AMFValidationReport] = {
    val fragment = createPayloadFragment(instanceUnit)
    config
      .elementClient()
      .payloadValidatorFor(schema, Mimes.`application/yaml`, ValidationMode.StrictValidationMode)
      .validate(fragment)
  }

  private def createPayloadFragment(unit: JsonLDInstanceDocument): PayloadFragment = {
    val ctx      = JsonLdSchemaContext(ParserContext(config = LimitedParseConfig(DefaultErrorHandler())))
    val encoded  = unit.encodes.head.asInstanceOf[JsonLDObject]
    val ast      = getSourceYNode(encoded).getOrElse(JsonLDInstanceRenderHelper.renderAsYNode(encoded))
    val dataNode = DataNodeParser(ast)(ctx).parse()
    PayloadFragment(dataNode, Mimes.`application/yaml`).withLocation(getLocation(unit))
  }

  private def getSourceYNode(obj: JsonLDObject): Option[YNode] = {
    obj.annotations.find(classOf[SourceYPart]).map(_.ast).flatMap {
      case map: YMap      => Some(YNode(map))
      case seq: YSequence => Some(YNode(seq))
      case _              => None
    }
  }

  private def getLocation(unit: JsonLDInstanceDocument): String = unit.location().getOrElse("")
}
