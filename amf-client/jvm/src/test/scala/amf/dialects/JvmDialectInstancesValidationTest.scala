package amf.dialects

class JvmDialectInstancesValidationTest extends DialectInstancesValidationTest {
  test("validation mule_config  example 1 incorrect") {
    validate("mule_config_dialect1.raml",
             "mule_config_instance_incorrect1.raml",
             Some("mule_config_instance_incorrect1.report.jvm.json"))
  }

  test("validation dialect 4 example 1 incorrect") {
    validate("dialect4.raml", "instance4_incorrect1.raml", Some("instance4_incorrect1.report.jvm.json"))
  }
}
