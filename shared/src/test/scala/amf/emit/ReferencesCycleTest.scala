package amf.emit

import amf.client.GenerationOptions
import amf.common.ListAssertions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.io.BuildCycleTests
import amf.remote._
import amf.validation.Validation

/**
  * Created by hernan.najles on 9/19/17.
  */
class ReferencesCycleTest extends BuildCycleTests with ListAssertions {

  override val basePath = "file://shared/src/test/resources/references/"

  private val fixture = Seq(
    "Simple library raml"                         -> ("libraries.raml", RamlYamlHint)                -> ("lib/lib.raml", Raml),
    "Simple library oas"                          -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json", Oas),
    "Library raml to oas"                         -> ("libraries.raml", RamlJsonHint)                -> ("lib/lib.raml.json", Oas),
    "Library oas to raml"                         -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json.raml", Raml),
    "Library raml to amf"                         -> ("libraries.raml", RamlJsonHint)                -> ("lib/lib.raml.jsonld", Amf),
    "Library oas to amf"                          -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json.jsonld", Amf),
    "Library amf to oas from include"             -> ("libraries.json.jsonld", AmfJsonHint)          -> ("lib/lib.json", Oas),
    "Library amf to raml from include"            -> ("libraries.raml.jsonld", AmfJsonHint)          -> ("lib/lib.raml", Raml),
    "Data type fragment raml to raml"             -> ("data-type-fragment.raml", RamlYamlHint)       -> ("fragments/person.raml", Raml),
    "Data type fragment oas to oas"               -> ("data-type-fragment.json", OasJsonHint)        -> ("fragments/person.json", Oas),
    "Data type fragment raml to oas"              -> ("data-type-fragment.raml", RamlJsonHint)       -> ("fragments/person.json", Oas),
    "Data type fragment oas to raml"              -> ("data-type-fragment.json", OasJsonHint)        -> ("fragments/person.raml", Raml),
    "Data type fragment amf to raml from include" -> ("data-type-fragment.raml.jsonld", AmfJsonHint) -> ("fragments/person.raml", Raml),
    "Data type fragment amf to oas from include"  -> ("data-type-fragment.json.jsonld", AmfJsonHint) -> ("fragments/person.json", Oas),
    "Resource type fragment raml to raml"         -> ("resource-type-fragment.raml", RamlYamlHint)   -> ("fragments/resource-type.raml", Raml),
    "Trait fragment raml to raml"                 -> ("trait-fragment.raml", RamlYamlHint)           -> ("fragments/trait.raml", Raml),
    "Alias library reference raml test"           -> ("lib-alias-reference.raml", RamlYamlHint)      -> ("lib/lib-declaration.raml", Raml),
    "Security schemes fragment raml to raml"      -> ("security-scheme-fragment.raml", RamlYamlHint) -> ("fragments/security-scheme.raml", Raml),
    "Security schemes fragment oas to oas"        -> ("security-scheme-fragment.json", OasJsonHint)  -> ("fragments/security-scheme.json", Oas),
    "Named Example fragment raml to raml"         -> ("named-example.raml", RamlYamlHint)            -> ("fragments/named-example.raml", Raml),
    "Named Example fragment oas to oas"           -> ("named-example.json", OasJsonHint)             -> ("fragments/named-example.json", Oas)
  )

  fixture.foreach {
    case ((title, (document, hint)), (reference, vendor)) =>
      test(title) {
        val validation = Validation(platform)
        AMFCompiler(basePath + document, platform, hint, validation)
          .build()
          .flatMap(renderReference(reference, vendor, _))
          .flatMap { a =>
            val file = basePath + reference
            platform.resolve(file, None).flatMap { expected =>
              platform.resolve("file://" + a, None).map { actual =>
                checkDiff(actual.stream.toString, actual.url, expected.stream.toString, expected.url)
              }
            }
          }
      }
  }

  private def renderReference(reference: String, vendor: Vendor, unit: BaseUnit) = {
    val ref = unit.references.head
    AMFDumper(ref, vendor, vendor.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
      .flatMap(platform.write("file://" + tmp(reference.replace("/", "--")), _))
  }

  case class ModuleContent(url: String, content: String)

}
