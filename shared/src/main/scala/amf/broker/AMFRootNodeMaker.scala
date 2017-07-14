package amf.broker

import amf.common.AMFASTNode
import amf.common.AMFToken._
import amf.domain.APIDocumentation
import amf.remote.{Amf, Oas, Raml, Vendor}

import scala.collection.mutable.ListBuffer

class AMFRootNodeMaker extends AMFTreeMaker[APIDocumentation] {

  def make(webApi: APIDocumentation, vendor: Vendor): AMFASTNode = {
    var rootChilds = vendor match {
      case Raml =>
        if (webApi.license != null) makeRamlContent(webApi) :+ LicenseTreeMaker(webApi.license)
        else makeRamlContent(webApi)
      case Oas => makeOasContent(webApi)

      case Amf => makeAmfContent(webApi)
      case _   => ???
    }

    if (webApi.provider != null) rootChilds = rootChilds :+ OrganizationTreeMaker(webApi.provider)
    if (webApi.documentation != null) rootChilds = rootChilds :+ CreativeWorkTreeMaker(webApi.documentation)

    new AMFASTNode(Root, "", null, List(new AMFASTNode(MapToken, "", null, rootChilds)))
  }

  def makeAmfContent(webApi: APIDocumentation): List[AMFASTNode] = {
    val children: ListBuffer[AMFASTNode] = new ListBuffer[AMFASTNode]

    children += makeJsonPropertyNode("http://schema.org/name", webApi.name)
    children += makeJsonPropertyNode("http://raml.org/vocabularies/http#host", webApi.host)

    children += makeJsonPropertyNode("http://raml.org/vocabularies/http#scheme", webApi.schemes)

    val nodes = List(makeJsonPropertyNode("http://raml.org/vocabularies/document#encodes", children.toList))
    nodes
  }

  def makeRamlContent(webApi: APIDocumentation): List[AMFASTNode] = {
    val children: ListBuffer[AMFASTNode] = new ListBuffer[AMFASTNode]
    children += makePropertyNode("title", webApi.name)
    children += makePropertyNode("description", webApi.description)
    children += makePropertyNode("mediaType", webApi.accepts)
    children += makePropertyNode("version", webApi.version)
    children += makePropertyNode("termsOfService", webApi.termsOfService)
    children += makePropertyNode("mediaType", webApi.accepts)

    children += makeListNode("protocols", webApi.schemes)
    children += makePropertyNode("baseUri", webApi.host)
    children.toList
  }

  def makeOasContent(webApi: APIDocumentation): List[AMFASTNode] = {
    //TODO

    val children: ListBuffer[AMFASTNode] = new ListBuffer[AMFASTNode]

    children += makePropertyNode("consumes", webApi.accepts)

    children += makePropertyNode("produces", webApi.contentType)
    children += makeListNode("schemes", webApi.schemes)
    children += makePropertyNode("basePath", webApi.host)
    children += makePropertyNode("host", webApi.host)

    var infoList: ListBuffer[(String, Any)] =
      ListBuffer(("title", webApi.name),
                 ("description", webApi.description),
                 ("version", webApi.version),
                 ("termsOfService", webApi.termsOfService))

    if (webApi.license != null)
      infoList += (("license", List(("url", webApi.license.url), ("name", webApi.license.name))))
    children += makeStruct(("info", infoList.toList))

    children.toList
//    List(new AMFASTNode(MapToken,"",null,children))
  }

}

object AMFRootNodeMaker {
  def apply(webApi: APIDocumentation, vendor: Vendor): AMFASTNode = new AMFRootNodeMaker().make(webApi, vendor)

}
