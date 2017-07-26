package amf.graph

import amf.common.Tests
import amf.emit.AMFUnitFixtureTest
import org.scalatest.FunSuite

/**
  * [[GraphEmitter]] test
  */
class GraphEmitterTest extends FunSuite with AMFUnitFixtureTest {

  test("Document encoding simple WebApi (using @context)") {
    val ast = GraphEmitter.emit(`document/api/bare`)
    Tests.checkDiff(
      ast.toString,
      """ |(Root {
          |    (Map {
          |        (Entry {
          |            (Str
          |              content -> "@context")
          |            (Map {
          |                (Entry {
          |                    (Str
          |                      content -> "raml-doc")
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/document#")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http")
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-shapes")
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/shapes#")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "hydra")
          |                    (Str
          |                      content -> "http://www.w3.org/ns/hydra/core#")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "shacl")
          |                    (Str
          |                      content -> "http://www.w3.org/ns/shacl#")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org")
          |                    (Str
          |                      content -> "http://schema.org/")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "xsd")
          |                    (Str
          |                      content -> "http://www.w3.org/2001/XMLSchema#")
          |                    })
          |                })
          |            })
          |        (Entry {
          |            (Str
          |              content -> "@type")
          |            (Seq {
          |                (Str
          |                  content -> "raml-doc:Document")
          |                (Str
          |                  content -> "raml-doc:Fragment")
          |                (Str
          |                  content -> "raml-doc:Module")
          |                (Str
          |                  content -> "raml-doc:Unit")
          |                })
          |            })
          |        (Entry {
          |            (Str
          |              content -> "raml-doc:encodes")
          |            (Map {
          |                (Entry {
          |                    (Str
          |                      content -> "@type")
          |                    (Seq {
          |                        (Str
          |                          content -> "schema-org:WebAPI")
          |                        (Str
          |                          content -> "raml-doc:DomainElement")
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:name")
          |                    (Str
          |                      content -> "test")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:description")
          |                    (Str
          |                      content -> "test description")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:host")
          |                    (Str
          |                      content -> "http://localhost.com/api")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:schemes")
          |                    (Seq {
          |                        (Str
          |                          content -> "http")
          |                        (Str
          |                          content -> "https")
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:basePath")
          |                    (Str
          |                      content -> "http://localhost.com/api")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:accepts")
          |                    (Str
          |                      content -> "application/json")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:contentType")
          |                    (Str
          |                      content -> "application/json")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:version")
          |                    (Str
          |                      content -> "1.1")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:termsOfService")
          |                    (Str
          |                      content -> "termsOfService")
          |                    })
          |                })
          |            })
          |        })
          |    })
      """.stripMargin
    )
  }

  test("Document encoding simple WebApi (expanded)") {
    val ast = GraphEmitter.emit(`document/api/bare`, expanded = true)
    Tests.checkDiff(
      ast.toString,
      """ |(Root {
          |    (Map {
          |        (Entry {
          |            (Str
          |              content -> "@type")
          |            (Seq {
          |                (Str
          |                  content -> "http://raml.org/vocabularies/document#Document")
          |                (Str
          |                  content -> "http://raml.org/vocabularies/document#Fragment")
          |                (Str
          |                  content -> "http://raml.org/vocabularies/document#Module")
          |                (Str
          |                  content -> "http://raml.org/vocabularies/document#Unit")
          |                })
          |            })
          |        (Entry {
          |            (Str
          |              content -> "http://raml.org/vocabularies/document#encodes")
          |            (Map {
          |                (Entry {
          |                    (Str
          |                      content -> "@type")
          |                    (Seq {
          |                        (Str
          |                          content -> "http://schema.org/WebAPI")
          |                        (Str
          |                          content -> "http://raml.org/vocabularies/document#DomainElement")
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://schema.org/name")
          |                    (Str
          |                      content -> "test")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://schema.org/description")
          |                    (Str
          |                      content -> "test description")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#host")
          |                    (Str
          |                      content -> "http://localhost.com/api")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#schemes")
          |                    (Seq {
          |                        (Str
          |                          content -> "http")
          |                        (Str
          |                          content -> "https")
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#basePath")
          |                    (Str
          |                      content -> "http://localhost.com/api")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#accepts")
          |                    (Str
          |                      content -> "application/json")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://raml.org/vocabularies/http#contentType")
          |                    (Str
          |                      content -> "application/json")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://schema.org/version")
          |                    (Str
          |                      content -> "1.1")
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "http://schema.org/termsOfService")
          |                    (Str
          |                      content -> "termsOfService")
          |                    })
          |                })
          |            })
          |        })
          |    })
      """.stripMargin
    )
  }
}
