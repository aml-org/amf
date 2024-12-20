package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.validation.ProfileName
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._

class ConstraintsConfigurationSetupTest extends ConfigurationSetupTest {

  case class ExpectedConstraintExistenceCase(config: AMFConfiguration, vendors: Seq[Spec])
  case class ErrorConstraintExistenceCase(config: AMFConfiguration, vendors: Seq[Spec])

  val validateFixtures: Seq[Any] = Seq(
    generateConstraintExistenceFixtures(apiConfig, Seq()),
    generateConstraintExistenceFixtures(webApiConfig, Seq()),
    generateConstraintExistenceFixtures(async20Config, Seq(ASYNC20)),
    generateConstraintExistenceFixtures(oasConfig, Seq()),
    generateConstraintExistenceFixtures(oas20Config, Seq(OAS20)),
    generateConstraintExistenceFixtures(oas30Config, Seq(OAS30)),
    generateConstraintExistenceFixtures(oas31Config, Seq(OAS31)),
    generateConstraintExistenceFixtures(ramlConfig, Seq()),
    generateConstraintExistenceFixtures(raml10Config, Seq(RAML10)),
    generateConstraintExistenceFixtures(raml08Config, Seq(RAML08))
  ).flatten

  validateFixtures.foreach {
    case f: ExpectedConstraintExistenceCase =>
      test(s"Test - config ${configNames(f.config)} only has constraints ${f.vendors}") {
        val assertion = onlyHasConstraintsOf(f.config, f.vendors)
        assertion shouldBe true
      }
    case e: ErrorConstraintExistenceCase =>
      test(s"Test - config ${configNames(e.config)} doesn't have constraints of ${e.vendors}") {
        val assertion = onlyHasConstraintsOf(e.config, e.vendors)
        assertion shouldBe false
      }
  }

  private def onlyHasConstraintsOf(config: AMFConfiguration, vendors: Seq[Spec]): Boolean = {
    val constraints = config.registry.getConstraintsRules
    constraints.size == vendors.length && vendors.forall(v => constraints.contains(ProfileName(v.id)))
  }

  private def generateConstraintExistenceFixtures(
      config: AMFConfiguration,
      expectedConstraintOwners: Seq[Spec]
  ): Seq[Any] = {
    val errorConfigs = vendors.diff(expectedConstraintOwners)
    Seq(
      ExpectedConstraintExistenceCase(config, expectedConstraintOwners),
      ErrorConstraintExistenceCase(config, errorConfigs)
    )
  }
}
