package amf.graph

import amf.common.Tests
import amf.emit.AMFUnitFixtureTest
import org.scalatest.FunSuite

/**
  * [[GraphEmitter]] test
  */
class GraphEmitterTest extends FunSuite with AMFUnitFixtureTest {

  test("Document encoding simple WebApi") {
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
          |              content -> "@id")
          |            (Str
          |              content -> "file:///tmp/test")
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
          |              content -> "raml-doc:location")
          |            (Str
          |              content -> "file:///tmp/test")
          |            })
          |        (Entry {
          |            (Str
          |              content -> "raml-doc:encodes")
          |            (Map {
          |                (Entry {
          |                    (Str
          |                      content -> "@id")
          |                    (Str
          |                      content -> "file:///tmp/test#/web-api")
          |                    })
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
        |              content -> "@id")
        |            (Str
        |              content -> "file:///tmp/test")
        |            })
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
        |              content -> "http://raml.org/vocabularies/document#location")
        |            (Map {
        |                (Entry {
        |                    (Str
        |                      content -> "@value")
        |                    (Str
        |                      content -> "file:///tmp/test")
        |                    })
        |                })
        |            })
        |        (Entry {
        |            (Str
        |              content -> "http://raml.org/vocabularies/document#encodes")
        |            (Map {
        |                (Entry {
        |                    (Str
        |                      content -> "@id")
        |                    (Str
        |                      content -> "file:///tmp/test#/web-api")
        |                    })
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
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "test")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://schema.org/description")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "test description")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/http#host")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "http://localhost.com/api")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/http#schemes")
        |                    (Seq {
        |                        (Map {
        |                            (Entry {
        |                                (Str
        |                                  content -> "@value")
        |                                (Str
        |                                  content -> "http")
        |                                })
        |                            })
        |                        (Map {
        |                            (Entry {
        |                                (Str
        |                                  content -> "@value")
        |                                (Str
        |                                  content -> "https")
        |                                })
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/http#basePath")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "http://localhost.com/api")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/http#accepts")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "application/json")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/http#contentType")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "application/json")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://schema.org/version")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "1.1")
        |                            })
        |                        })
        |                    })
        |                (Entry {
        |                    (Str
        |                      content -> "http://schema.org/termsOfService")
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@value")
        |                            (Str
        |                              content -> "termsOfService")
        |                            })
        |                        })
        |                    })
        |                })
        |            })
        |        })
        |    })
      """.stripMargin
    )
  }

  test("Document encoding full WebApi") {
    val ast = GraphEmitter.emit(`document/api/full`)
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
          |              content -> "@id")
          |            (Str
          |              content -> "file:///tmp/test")
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
          |              content -> "raml-doc:location")
          |            (Str
          |              content -> "file:///tmp/test")
          |            })
          |        (Entry {
          |            (Str
          |              content -> "raml-doc:encodes")
          |            (Map {
          |                (Entry {
          |                    (Str
          |                      content -> "@id")
          |                    (Str
          |                      content -> "file:///tmp/test#/web-api")
          |                    })
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
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:provider")
          |                    (Map {
          |                        (Entry {
          |                            (Str
          |                              content -> "@id")
          |                            (Str
          |                              content -> "file:///tmp/test#/web-api/organization")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "@type")
          |                            (Seq {
          |                                (Str
          |                                  content -> "schema-org:Organization")
          |                                (Str
          |                                  content -> "raml-doc:DomainElement")
          |                                })
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:email")
          |                            (Str
          |                              content -> "test@test")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:name")
          |                            (Str
          |                              content -> "organizationName")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:url")
          |                            (Str
          |                              content -> "organizationUrl")
          |                            })
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:license")
          |                    (Map {
          |                        (Entry {
          |                            (Str
          |                              content -> "@id")
          |                            (Str
          |                              content -> "file:///tmp/test#/web-api/license")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "@type")
          |                            (Seq {
          |                                (Str
          |                                  content -> "raml-http:License")
          |                                (Str
          |                                  content -> "raml-doc:DomainElement")
          |                                })
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:name")
          |                            (Str
          |                              content -> "licenseName")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:url")
          |                            (Str
          |                              content -> "licenseUrl")
          |                            })
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "schema-org:documentation")
          |                    (Map {
          |                        (Entry {
          |                            (Str
          |                              content -> "@id")
          |                            (Str
          |                              content -> "file:///tmp/test#/web-api/creative-work")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "@type")
          |                            (Seq {
          |                                (Str
          |                                  content -> "schema-org:CreativeWork")
          |                                (Str
          |                                  content -> "raml-doc:DomainElement")
          |                                })
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:url")
          |                            (Str
          |                              content -> "creativoWorkUrl")
          |                            })
          |                        (Entry {
          |                            (Str
          |                              content -> "schema-org:description")
          |                            (Str
          |                              content -> "creativeWorkDescription")
          |                            })
          |                        })
          |                    })
          |                (Entry {
          |                    (Str
          |                      content -> "raml-http:endpoint")
          |                    (Seq {
          |                        (Map {
          |                            (Entry {
          |                                (Str
          |                                  content -> "@id")
          |                                (Str
          |                                  content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint")
          |                                })
          |                            (Entry {
          |                                (Str
          |                                  content -> "@type")
          |                                (Seq {
          |                                    (Str
          |                                      content -> "raml-http:EndPoint")
          |                                    (Str
          |                                      content -> "raml-doc:DomainElement")
          |                                    })
          |                                })
          |                            (Entry {
          |                                (Str
          |                                  content -> "schema-org:description")
          |                                (Str
          |                                  content -> "test endpoint")
          |                                })
          |                            (Entry {
          |                                (Str
          |                                  content -> "schema-org:name")
          |                                (Str
          |                                  content -> "endpoint")
          |                                })
          |                            (Entry {
          |                                (Str
          |                                  content -> "raml-http:path")
          |                                (Str
          |                                  content -> "/endpoint")
          |                                })
          |                            (Entry {
          |                                (Str
          |                                  content -> "hydra:supportedOperation")
          |                                (Seq {
          |                                    (Map {
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "@id")
          |                                            (Str
          |                                              content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/get")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "@type")
          |                                            (Seq {
          |                                                (Str
          |                                                  content -> "hydra:Operation")
          |                                                (Str
          |                                                  content -> "raml-doc:DomainElement")
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:description")
          |                                            (Str
          |                                              content -> "test operation get")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:documentation")
          |                                            (Map {
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "@id")
          |                                                    (Str
          |                                                      content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/get/creative-work")
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "@type")
          |                                                    (Seq {
          |                                                        (Str
          |                                                          content -> "schema-org:CreativeWork")
          |                                                        (Str
          |                                                          content -> "raml-doc:DomainElement")
          |                                                        })
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "schema-org:description")
          |                                                    (Str
          |                                                      content -> "documentation operation")
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "schema-org:url")
          |                                                    (Str
          |                                                      content -> "localhost:8080/endpoint/operation")
          |                                                    })
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "hydra:method")
          |                                            (Str
          |                                              content -> "get")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:name")
          |                                            (Str
          |                                              content -> "test get")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "raml-http:scheme")
          |                                            (Seq {
          |                                                (Str
          |                                                  content -> "http")
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "raml-http:guiSummary")
          |                                            (Str
          |                                              content -> "summary of operation get")
          |                                            })
          |                                        })
          |                                    (Map {
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "@id")
          |                                            (Str
          |                                              content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/post")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "@type")
          |                                            (Seq {
          |                                                (Str
          |                                                  content -> "hydra:Operation")
          |                                                (Str
          |                                                  content -> "raml-doc:DomainElement")
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "hydra:method")
          |                                            (Str
          |                                              content -> "post")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:description")
          |                                            (Str
          |                                              content -> "test operation post")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "raml-doc:deprecated")
          |                                            (Bool
          |                                              content -> true)
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:documentation")
          |                                            (Map {
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "@id")
          |                                                    (Str
          |                                                      content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/post/creative-work")
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "@type")
          |                                                    (Seq {
          |                                                        (Str
          |                                                          content -> "schema-org:CreativeWork")
          |                                                        (Str
          |                                                          content -> "raml-doc:DomainElement")
          |                                                        })
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "schema-org:description")
          |                                                    (Str
          |                                                      content -> "documentation operation")
          |                                                    })
          |                                                (Entry {
          |                                                    (Str
          |                                                      content -> "schema-org:url")
          |                                                    (Str
          |                                                      content -> "localhost:8080/endpoint/operation")
          |                                                    })
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "schema-org:name")
          |                                            (Str
          |                                              content -> "test post")
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "raml-http:scheme")
          |                                            (Seq {
          |                                                (Str
          |                                                  content -> "http")
          |                                                })
          |                                            })
          |                                        (Entry {
          |                                            (Str
          |                                              content -> "raml-http:guiSummary")
          |                                            (Str
          |                                              content -> "summary of operation post")
          |                                            })
          |                                        })
          |                                    })
          |                                })
          |                            })
          |                        })
          |                    })
          |                })
          |            })
          |        })
          |    })
        """.stripMargin
    )
  }
}
