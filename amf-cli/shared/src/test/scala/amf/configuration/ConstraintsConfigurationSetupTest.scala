package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.validation.ProfileName
import amf.core.internal.remote.Vendor
import amf.core.internal.remote.Vendor._

class ConstraintsConfigurationSetupTest extends ConfigurationSetupTest {

  case class ConstraintExistenceFixture(config: AMFConfiguration, vendors: Seq[Vendor])
  case class ErrorConstraintExistenceFixture(config: AMFConfiguration, vendors: Seq[Vendor])

  val validateFixtures: Seq[Any] = Seq(
    generateConstraintExistenceFixtures(apiConfig, Seq(RAML10, RAML08, OAS20, OAS30, ASYNC20)),
    generateConstraintExistenceFixtures(webApiConfig, Seq(RAML10, RAML08, OAS20, OAS30)),
    generateConstraintExistenceFixtures(async20Config, Seq(ASYNC20)),
    generateConstraintExistenceFixtures(oasConfig, Seq(OAS20, OAS30)),
    generateConstraintExistenceFixtures(oas20Config, Seq(OAS20)),
    generateConstraintExistenceFixtures(oas30Config, Seq(OAS30)),
    generateConstraintExistenceFixtures(ramlConfig, Seq(RAML10, RAML08)),
    generateConstraintExistenceFixtures(raml10Config, Seq(RAML10)),
    generateConstraintExistenceFixtures(raml08Config, Seq(RAML08))
  ).flatten

  validateFixtures.foreach {
    case f: ConstraintExistenceFixture =>
      test(s"Test - config ${configNames(f.config)} only has constraints ${f.vendors}") {
        val assertion = onlyHasConstraintsOf(f.config, f.vendors)
        assertion shouldBe true
      }
    case e: ErrorConstraintExistenceFixture =>
      test(s"Test - config ${configNames(e.config)} doesn't have constraints of ${e.vendors}") {
        val assertion = onlyHasConstraintsOf(e.config, e.vendors)
        assertion shouldBe false
      }
  }

  private def onlyHasConstraintsOf(config: AMFConfiguration, vendors: Seq[Vendor]): Boolean = {
    val constraints = config.registry.constraintsRules
    constraints.size == vendors.length && vendors.forall(v => constraints.contains(ProfileName(v.name)))
  }

  private def generateConstraintExistenceFixtures(config: AMFConfiguration,
                                                  expectedConstraintOwners: Seq[Vendor]): Seq[Any] = {
    val errorConfigs = vendors.diff(expectedConstraintOwners)
    Seq(ConstraintExistenceFixture(config, expectedConstraintOwners),
        ErrorConstraintExistenceFixture(config, errorConfigs))
  }
}
