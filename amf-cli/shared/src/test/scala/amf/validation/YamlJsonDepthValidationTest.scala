package amf.validation

import amf.apicontract.client.scala.{OASConfiguration, RAMLConfiguration}
import amf.client.validation.PayloadValidationUtils
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.ValidationMode.StrictValidationMode
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.lexer.BaseLexer
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

trait YamlJsonDepthValidationTest extends AsyncFunSuite with Matchers with PayloadValidationUtils {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  protected val limit                                        = 500
  protected val ramlConfigDefault                            = RAMLConfiguration.RAML10()
  protected val ramlConfig = RAMLConfiguration.RAML10().withParsingOptions(ParsingOptions().setMaxJsonYamlDepth(limit))
  protected val oasConfig  = OASConfiguration.OAS20().withParsingOptions(ParsingOptions().setMaxJsonYamlDepth(limit))

  protected val nestedDepthYAML: String =
    """{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{
      |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
      |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
      |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
      |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
      |{{{{{[{{{{{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
      |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
      |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
      |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
      |{{{{{[{{{{{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
      |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
      |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
      |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
      |{{{{{[{{{{{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{""".stripMargin

  protected val nestedDepthJSON: String =
    """[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[
      |[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[""".stripMargin

  protected def assertThresholdViolation(
      results: Seq[AMFValidationResult],
      expectedMessage: String
  ): Assertion = {
    assert(results.size == 1)
    assert(results.head.message == expectedMessage)
  }

  test("payload validation YAML") {

    val validator =
      ramlConfig.elementClient().payloadValidatorFor(ScalarShape(), `application/yaml`, StrictValidationMode)

    val report = validator.validate(nestedDepthYAML)

    report.map { r =>
      assertThresholdViolation(r.results, s"Reached maximum depth value of $limit")
    }
  }

  test("payload validation YAML with default limit") {

    val validator =
      ramlConfigDefault.elementClient().payloadValidatorFor(ScalarShape(), `application/yaml`, StrictValidationMode)

    val report = validator.validate(nestedDepthYAML)

    report.map { r =>
      assertThresholdViolation(r.results, s"Reached maximum depth value of ${BaseLexer.DEFAULT_MAX_DEPTH}")
    }
  }

  test("parsing and resolution violation - raml resolution with examples") {
    val api =
      """#%RAML 1.0
        |title: Something
        |
        |/endpoint:
        |  get:
        |    body:
        |      application/json:
        |        example: |
        |          {{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{
        |          [{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
        |          {{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
        |          {{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
        |          {[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
        |          {{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
        |          {{{{{[{{{{{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
        |          [{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
        |          {{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
        |          {{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
        |          {[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
        |          {{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
        |          {{{{{[{{{{{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{""".stripMargin

    val client = ramlConfig.baseUnitClient()
    for {
      parseResult      <- client.parseContent(api)
      unit             <- Future.successful(parseResult.baseUnit)
      validationReport <- client.validate(unit)
      _                <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      parseResult.results.nonEmpty shouldBe true
      parseResult.results.exists(r => r.message == s"Reached maximum depth value of $limit") shouldBe true
      validationReport.results.isEmpty shouldBe true
    }
  }

}
