package amf.plugins

import amf.apicontract.internal.spec.jsonschema.{JsonSchemaEntry, JsonSchemaParsePlugin}
import org.scalatest.funsuite.{AnyFunSuite, AsyncFunSuite}
import org.scalatest.matchers.should.Matchers

class JsonSchemaEntryTest extends AsyncFunSuite with Matchers {

  test("Draft should be recognizable with https") {
    val drafts = Seq(
      "https://json-schema.org/draft-03/schema#",
      "https://json-schema.org/draft-04/schema#",
      "https://json-schema.org/draft-06/schema#",
      "https://json-schema.org/draft-07/schema#",
      "https://json-schema.org/draft/2019-09/schema#"
    )
    drafts forall (draft => JsonSchemaEntry(draft).isDefined) shouldBe true
  }

  test("Draft should be recognizable without trailing #") {
    val drafts = Seq(
      "http://json-schema.org/draft-03/schema",
      "http://json-schema.org/draft-04/schema",
      "http://json-schema.org/draft-06/schema",
      "http://json-schema.org/draft-07/schema",
      "http://json-schema.org/draft/2019-09/schema"
    )
    drafts forall (draft => JsonSchemaEntry(draft).isDefined) shouldBe true
  }
}
