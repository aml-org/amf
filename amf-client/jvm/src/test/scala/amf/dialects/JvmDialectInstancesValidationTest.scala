package amf.dialects

class JvmDialectInstancesValidationTest extends DialectInstancesValidationTest {
  test("validation mule_config  example 1 incorrect") {
    validate("mule_config_dialect1.yaml",
             "mule_config_instance_incorrect1.yaml",
             Some("mule_config_instance_incorrect1.report.jvm.json"))
  }

  test("validation dialect 4 example 1 incorrect") {
    validate("dialect4.yaml", "instance4_incorrect1.yaml", Some("instance4_incorrect1.report.jvm.json"))
  }
}
