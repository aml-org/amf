package amf.configuration
import amf.apicontract.client.platform.{
  AMFConfiguration,
  APIConfiguration,
  OASConfiguration,
  RAMLConfiguration,
  WebAPIConfiguration
}
import amf.apicontract.client.scala.UnrecognizedSpecException
import amf.core.internal.remote.Spec

// This suite tests configuration setups from each individual platform
trait PlatformConfigurationTest extends ConfigurationSetupTest {
  protected val all: Set[Spec] = Set(
    Spec.RAML08,
    Spec.RAML10,
    Spec.OAS20,
    Spec.OAS30,
    Spec.ASYNC20,
    Spec.PAYLOAD,
    Spec.JSONSCHEMA,
    Spec.AMF,
    Spec.GRPC
  )
  protected val acceptedWebApiSpecs: Set[Spec] = Set(Spec.RAML08, Spec.RAML10, Spec.OAS20, Spec.OAS30)
  protected val acceptedApiSpecs: Set[Spec]    = acceptedWebApiSpecs + Spec.ASYNC20
  protected val acceptedOasSpecs: Set[Spec]    = Set(Spec.OAS20, Spec.OAS30)
  protected val acceptedRamlSpecs: Set[Spec]   = Set(Spec.RAML08, Spec.RAML10)

  acceptedWebApiSpecs.foreach { spec =>
    test(s"Config from ${spec.id} is constructed from WebAPIConfiguration") {
      assert(WebAPIConfiguration.fromSpec(spec).isInstanceOf[AMFConfiguration])
    }
  }

  all.diff(acceptedWebApiSpecs).foreach { spec =>
    test(s"Config from ${spec.id} cannot be constructed from WebAPIConfiguration") {
      assertThrows[UnrecognizedSpecException] {
        WebAPIConfiguration.fromSpec(spec)
      }
    }
  }

  acceptedApiSpecs.foreach { spec =>
    test(s"Config from ${spec.id} is constructed from APIConfiguration") {
      assert(APIConfiguration.fromSpec(spec).isInstanceOf[AMFConfiguration])
    }
  }

  all.diff(acceptedApiSpecs).foreach { spec =>
    test(s"Config from ${spec.id} cannot be constructed from APIConfiguration") {
      assertThrows[UnrecognizedSpecException] {
        APIConfiguration.fromSpec(spec)
      }
    }
  }

  acceptedOasSpecs.foreach { spec =>
    test(s"Config from ${spec.id} is constructed from OASConfiguration") {
      assert(OASConfiguration.fromSpec(spec).isInstanceOf[AMFConfiguration])
    }
  }

  all.diff(acceptedOasSpecs).foreach { spec =>
    test(s"Config from ${spec.id} cannot be constructed from OASConfiguration") {
      assertThrows[UnrecognizedSpecException] {
        OASConfiguration.fromSpec(spec)
      }
    }
  }

  acceptedRamlSpecs.foreach { spec =>
    test(s"Config from ${spec.id} is constructed from RAMLConfiguration") {
      assert(RAMLConfiguration.fromSpec(spec).isInstanceOf[AMFConfiguration])
    }
  }

  all.diff(acceptedRamlSpecs).foreach { spec =>
    test(s"Config from ${spec.id} cannot be constructed from RAMLConfiguration") {
      assertThrows[UnrecognizedSpecException] {
        RAMLConfiguration.fromSpec(spec)
      }
    }
  }

}
