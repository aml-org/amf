package amf.client.model.domain

import amf.apicontract.client.platform.model.domain.security._
import amf.apicontract.client.scala.config.APIConfiguration
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.ScalarNode
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class SettingsTest extends FunSuite with Matchers with BeforeAndAfterAll {

  val s                                    = "test string"
  val clientStringList: ClientList[String] = Seq(s).asClient

  override protected def beforeAll(): Unit = {
    APIConfiguration.API() // TODO: ARM remove after wrappers are deleted
  }

  test("test Settings") {
    val node = new ScalarNode()

    val settings = new Settings()
      .withAdditionalProperties(node)
    settings.additionalProperties._internal shouldBe node._internal
  }

  test("test OAuth1Settings") {
    val settings = new OAuth1Settings()
      .withRequestTokenUri(s)
      .withAuthorizationUri(s)
      .withTokenCredentialsUri(s)
      .withSignatures(clientStringList)
    settings.requestTokenUri.value() shouldBe s
    settings.authorizationUri.value() shouldBe s
    settings.tokenCredentialsUri.value() shouldBe s
    settings.signatures.toString shouldBe clientStringList.toString
  }

  test("test OAuth2Settings") {
    val flows = Seq(new OAuth2Flow()._internal)

    val settings = new OAuth2Settings()
      .withFlows(flows.asClient)
      .withAuthorizationGrants(clientStringList)
    settings.flows.asInternal shouldBe flows
    settings.authorizationGrants.toString shouldBe clientStringList.toString
  }

  test("test ApiKeySettings") {
    val settings = new ApiKeySettings()
      .withName(s)
      .withIn(s)
    settings.name.value() shouldBe s
    settings.in.value() shouldBe s
  }

  test("test HttpApiKeySettings") {
    val settings = new HttpApiKeySettings()
      .withName(s)
      .withIn(s)
    settings.name.value() shouldBe s
    settings.in.value() shouldBe s
  }

  test("test HttpSettings") {
    val settings = new HttpSettings()
      .withScheme(s)
      .withBearerFormat(s)
    settings.scheme.value() shouldBe s
    settings.bearerFormat.value() shouldBe s
  }

  test("test OpenIdConnectSettings") {
    val scopes = Seq(new Scope()._internal)

    val settings = new OpenIdConnectSettings()
      .withUrl(s)
      .withScopes(scopes.asClient)
    settings.url.value() shouldBe s
    settings.scopes.asInternal shouldBe scopes
  }
}
