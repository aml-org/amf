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
        |    (Seq {
        |        (Map {
        |            (Entry {
        |                (Str
        |                  content -> "@id")
        |                (Str
        |                  content -> "file:///tmp/test")
        |                })
        |            (Entry {
        |                (Str
        |                  content -> "@type")
        |                (Seq {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Document")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Fragment")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Module")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Unit")
        |                    })
        |                })
        |            (Entry {
        |                (Str
        |                  content -> "http://raml.org/vocabularies/document#encodes")
        |                (Seq {
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@id")
        |                            (Str
        |                              content -> "file:///tmp/test#/web-api")
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "@type")
        |                            (Seq {
        |                                (Str
        |                                  content -> "http://schema.org/WebAPI")
        |                                (Str
        |                                  content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/name")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "test")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/description")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "test description")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#host")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http://localhost.com/api")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#schemes")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http")
        |                                        })
        |                                    })
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "https")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#basePath")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http://localhost.com/api")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#accepts")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "application/json")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#contentType")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "application/json")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/version")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "1.1")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/termsOfService")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "termsOfService")
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

  test("Document encoding full WebApi") {
    val ast = GraphEmitter.emit(`document/api/full`)
    Tests.checkDiff(
      ast.toString,
      """ |(Root {
        |    (Seq {
        |        (Map {
        |            (Entry {
        |                (Str
        |                  content -> "@id")
        |                (Str
        |                  content -> "file:///tmp/test")
        |                })
        |            (Entry {
        |                (Str
        |                  content -> "@type")
        |                (Seq {
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Document")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Fragment")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Module")
        |                    (Str
        |                      content -> "http://raml.org/vocabularies/document#Unit")
        |                    })
        |                })
        |            (Entry {
        |                (Str
        |                  content -> "http://raml.org/vocabularies/document#encodes")
        |                (Seq {
        |                    (Map {
        |                        (Entry {
        |                            (Str
        |                              content -> "@id")
        |                            (Str
        |                              content -> "file:///tmp/test#/web-api")
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "@type")
        |                            (Seq {
        |                                (Str
        |                                  content -> "http://schema.org/WebAPI")
        |                                (Str
        |                                  content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/name")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "test")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/description")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "test description")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#host")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http://localhost.com/api")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#schemes")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http")
        |                                        })
        |                                    })
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "https")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#basePath")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "http://localhost.com/api")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#accepts")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "application/json")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#contentType")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "application/json")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/version")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "1.1")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/termsOfService")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@value")
        |                                        (Str
        |                                          content -> "termsOfService")
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/provider")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@id")
        |                                        (Str
        |                                          content -> "file:///tmp/test#/web-api/organization")
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@type")
        |                                        (Seq {
        |                                            (Str
        |                                              content -> "http://schema.org/Organization")
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/url")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@id")
        |                                                    (Str
        |                                                      content -> "organizationUrl")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/name")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "organizationName")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/email")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "test@test")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/license")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@id")
        |                                        (Str
        |                                          content -> "file:///tmp/test#/web-api/license")
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@type")
        |                                        (Seq {
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/http#License")
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/url")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@id")
        |                                                    (Str
        |                                                      content -> "licenseUrl")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/name")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "licenseName")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://schema.org/documentation")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@id")
        |                                        (Str
        |                                          content -> "file:///tmp/test#/web-api/creative-work")
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@type")
        |                                        (Seq {
        |                                            (Str
        |                                              content -> "http://schema.org/CreativeWork")
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/url")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@id")
        |                                                    (Str
        |                                                      content -> "creativoWorkUrl")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/description")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "creativeWorkDescription")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    })
        |                                })
        |                            })
        |                        (Entry {
        |                            (Str
        |                              content -> "http://raml.org/vocabularies/http#endpoint")
        |                            (Seq {
        |                                (Map {
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@id")
        |                                        (Str
        |                                          content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint")
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "@type")
        |                                        (Seq {
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/http#EndPoint")
        |                                            (Str
        |                                              content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://raml.org/vocabularies/http#path")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "/endpoint")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/name")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "endpoint")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://schema.org/description")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@value")
        |                                                    (Str
        |                                                      content -> "test endpoint")
        |                                                    })
        |                                                })
        |                                            })
        |                                        })
        |                                    (Entry {
        |                                        (Str
        |                                          content -> "http://www.w3.org/ns/hydra/core#supportedOperation")
        |                                        (Seq {
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@id")
        |                                                    (Str
        |                                                      content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/get")
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@type")
        |                                                    (Seq {
        |                                                        (Str
        |                                                          content -> "http://www.w3.org/ns/hydra/core#Operation")
        |                                                        (Str
        |                                                          content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://www.w3.org/ns/hydra/core#method")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "get")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/name")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "test get")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/description")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "test operation get")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://raml.org/vocabularies/http#guiSummary")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "summary of operation get")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/documentation")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@id")
        |                                                                (Str
        |                                                                  content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/get/creative-work")
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@type")
        |                                                                (Seq {
        |                                                                    (Str
        |                                                                      content -> "http://schema.org/CreativeWork")
        |                                                                    (Str
        |                                                                      content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                                                    })
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "http://schema.org/url")
        |                                                                (Seq {
        |                                                                    (Map {
        |                                                                        (Entry {
        |                                                                            (Str
        |                                                                              content -> "@id")
        |                                                                            (Str
        |                                                                              content -> "localhost:8080/endpoint/operation")
        |                                                                            })
        |                                                                        })
        |                                                                    })
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "http://schema.org/description")
        |                                                                (Seq {
        |                                                                    (Map {
        |                                                                        (Entry {
        |                                                                            (Str
        |                                                                              content -> "@value")
        |                                                                            (Str
        |                                                                              content -> "documentation operation")
        |                                                                            })
        |                                                                        })
        |                                                                    })
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://raml.org/vocabularies/http#scheme")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "http")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                })
        |                                            (Map {
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@id")
        |                                                    (Str
        |                                                      content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/post")
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "@type")
        |                                                    (Seq {
        |                                                        (Str
        |                                                          content -> "http://www.w3.org/ns/hydra/core#Operation")
        |                                                        (Str
        |                                                          content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://www.w3.org/ns/hydra/core#method")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "post")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/name")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "test post")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/description")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "test operation post")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://raml.org/vocabularies/document#deprecated")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Bool
        |                                                                  content -> true)
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://raml.org/vocabularies/http#guiSummary")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "summary of operation post")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://schema.org/documentation")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@id")
        |                                                                (Str
        |                                                                  content -> "file:///tmp/test#/web-api/end-points/%2Fendpoint/post/creative-work")
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@type")
        |                                                                (Seq {
        |                                                                    (Str
        |                                                                      content -> "http://schema.org/CreativeWork")
        |                                                                    (Str
        |                                                                      content -> "http://raml.org/vocabularies/document#DomainElement")
        |                                                                    })
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "http://schema.org/url")
        |                                                                (Seq {
        |                                                                    (Map {
        |                                                                        (Entry {
        |                                                                            (Str
        |                                                                              content -> "@id")
        |                                                                            (Str
        |                                                                              content -> "localhost:8080/endpoint/operation")
        |                                                                            })
        |                                                                        })
        |                                                                    })
        |                                                                })
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "http://schema.org/description")
        |                                                                (Seq {
        |                                                                    (Map {
        |                                                                        (Entry {
        |                                                                            (Str
        |                                                                              content -> "@value")
        |                                                                            (Str
        |                                                                              content -> "documentation operation")
        |                                                                            })
        |                                                                        })
        |                                                                    })
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                (Entry {
        |                                                    (Str
        |                                                      content -> "http://raml.org/vocabularies/http#scheme")
        |                                                    (Seq {
        |                                                        (Map {
        |                                                            (Entry {
        |                                                                (Str
        |                                                                  content -> "@value")
        |                                                                (Str
        |                                                                  content -> "http")
        |                                                                })
        |                                                            })
        |                                                        })
        |                                                    })
        |                                                })
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
