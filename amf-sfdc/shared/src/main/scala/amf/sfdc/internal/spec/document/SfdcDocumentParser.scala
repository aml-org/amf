package amf.sfdc.internal.spec.document

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain._
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.client.scala.model.domain.{ScalarNode, Shape}
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.SearchScope
import amf.sfdc.internal.spec.context.SfdcWebApiContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnresolvedShape}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.YNodeLike.toString
import org.yaml.model._

import scala.:+
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class SfdcDocumentParser(root: Root)(implicit val ctx: SfdcWebApiContext) {

  val doc: Document = Document()
  def webapi: WebApi = doc.encodes.asInstanceOf[WebApi]

  protected def parseObjectRange(n: YNode, literalReference: String, paired : (mutable.Queue[String], mutable.Set[String]))(implicit ctx: SfdcWebApiContext): AnyShape = {
    // val topLevelAlias = ctx.topLevelPackageRef(literalReference).map(alias => Seq(alias)).getOrElse(Nil)
    // val qualifiedReference = ctx.fullMessagePath(literalReference)
    val externalReference = s".${literalReference}" // absolute reference based on the assumption the reference is for an external package imported in the file
    ctx.declarations
      .findType(literalReference, SearchScope.All) // local reference inside a nested message, transformed into a top-level for possibly nested type
    match {
      case Some(s: NodeShape) => {
        println("Link: " + literalReference)
        s.link(literalReference) // .asInstanceOf[NodeShape].withName(literalReference)
      }
      case Some(s: ScalarShape) =>
        s.link(literalReference) // .asInstanceOf[ScalarShape].withName(literalReference)
      case _ =>
        // println("Unresolved: " + literalReference)
        val shape = UnresolvedShape(literalReference)
        if (!paired._2.contains(literalReference)){
          paired._1 += literalReference
          paired._2 += literalReference
        }
        shape.withContext(ctx)
        shape.unresolved(literalReference, Seq(), Some(new SourceLocation(n.location.sourceName, 0, 0,
          n.location.lineFrom, n.location.columnFrom, n.location.lineTo, n.location.columnTo)))
        shape
    }
  }

  def addToDeclarations(currNode : Map[YNode,YNode], paired : (mutable.Queue[String], mutable.Set[String])) {
    // val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
    val path = currNode.get("url").get.value.toString
    val name = currNode.get("name").get
    val requiredFields = if (currNode.contains("requiredList")) {
      val preSeq = currNode.get("requiredList").get.asInstanceOf[YNodePlain].value
      if (preSeq.isInstanceOf[YScalar] && preSeq.asInstanceOf[YScalar].value == null) {
        Seq[String]()
      } else
        preSeq.asInstanceOf[YSequence].nodes.map(n => n.value.asInstanceOf[YScalar].text)
    } else Seq[String]()

    // currNode.getOrElse("requiredList", Seq[String]()).asInstanceOf[Seq[String]]
    // if (currNode.contains("requiredList")) currNode.get("requiredList").get.asInstanceOf[List[String]] else new ListBuffer[String]()
    if (!ctx.declarations.shapes.contains(name)) {
      val preschema = currNode.get("structureInfo").get.value.asInstanceOf[YMap].map.get("fields").get.value.asInstanceOf[YSequence].nodes
      val schema = preschema
        .foldLeft(Seq[PropertyShape]())((props, field) => {
          val fmap = field.value.asInstanceOf[YMap].map
          val propName = fmap.get("name").get.value.asInstanceOf[YScalar].text
          // val propName = prename.substring(1, prename.length - 1)

          val required = requiredFields.contains(propName) // if (fmap.get("nillable").get.value.toString == "false") true else false
          val multiple = if (fmap.get("unique").get.value.toString == "false") false else true
          val pretyper = fmap.get("type").get.value.toString
          val typer = pretyper.substring(1, pretyper.length - 1)
          val baseShape = PropertyShape().withName(propName).withMinCount(if (required) 1 else 0).withMaxCount(if (multiple) 1000 else 1)
          typer match {
            case "picklist" | "multipicklist" => {
              props :+ baseShape.withRange(ScalarShape().withValues(
                fmap.get("picklistValues") match {
                  case Some(pickles) => {
                    val fullList = pickles.children(0).children
                      .map(p => p.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map("value").value.asInstanceOf[YScalar].text)
                    val uniqueList = fullList.distinct.sorted
                    // pickles.children(0).children.map
                    /*
                    if (uniqueList.length > 10){
                      if (!ctx.declarations.shapes.contains(propName)){

                        ctx.declarations += ScalarShape().withId("http://salesforce.com/" + propName).withName(propName)
                          .withValues(uniqueList.map(p =>
                            ScalarNode(p, Some(XsdTypes.xsdString.iri()))))


                      }
                    }

                     */
                    uniqueList.map(p =>
                      ScalarNode(p, Some(XsdTypes.xsdString.iri()))
                    )
                  }
                  case None => Seq()
                }
              ))
            }
            case "id" | "string" | "textarea" | "url" | "phone" | "address" | "email" | "anyType" |
                 "currency" | "complexvalue" | "json" | "encryptedstring" | "combobox" | "base64" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
            }
            case "boolean" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#boolean"))
            }
            case "datetime" | "date" | "time" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#dateTime"))
            }
            case "double" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#double"))
            }
            case "int" | "long" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#integer"))
            }
            case "percent" => {
              props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#double"))
            }
            case "reference" => {
              try {
                fmap.get("referenceTo") match {
                  case Some(ynode) => {
                    val refName = ynode.value.asInstanceOf[YSequence].nodes(0).value.asInstanceOf[YScalar].text
                    val unresolved = parseObjectRange(field, refName, paired)
                    props :+ baseShape.withRange(unresolved)
                  }
                  case None =>
                    props :+ baseShape.withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
                }
              } catch {
                case e: Exception => {
                  println(name + " -- " + propName)
                  e.printStackTrace()
                  props
                }
              }
            }
            case _ => {
              println("No match for " + typer)
              props
            }
          }
        })
      val postSchema = NodeShape().withName(name).withProperties(schema)

      ctx.declarations += postSchema.add(DeclaredElement())
    }
  }
  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[SyamlParsedDocument].document.node; //root.parsed.asInstanceOf[SyamlParsedDocument].document
    val preBase = ast.value.asInstanceOf[YSequence].nodes(0).value.asInstanceOf[YMap].map.get("url").get.value.asInstanceOf[YScalar].text
    val baseUrl = preBase.substring(0, preBase.lastIndexOf('/') + 1)
    parseWebAPI(baseUrl)
    ctx.declarations += NodeShape().add(DeclaredElement()).withName("PostResponse").withProperties(
      Seq(PropertyShape().withName("id").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")),
        // PropertyShape().withName("errors").withName(ArrayShape().withLinkTarget()),
        PropertyShape().withName("success").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#boolean"))))

    val typeMap = ast.children(0).children.foldLeft(new HashMap[String, Map[YNode,YNode]]())((a,n) => {
      val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
      val name = currNode.get("name").get.asInstanceOf[YNodePlain].value.asInstanceOf[YScalar].text
      a + (name -> currNode)
    })
    val toAdd = new mutable.Queue[String]
    val seen = new mutable.HashSet[String]
    val starters = ast.children(0).children

      .filter(n => {
        val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
        val name = currNode.get("name").get.asInstanceOf[YNodePlain].value.asInstanceOf[YScalar].text
        name == "Account"
      })

    toAdd ++= starters.map(n => {
      val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
      val name = currNode.get("name").get.asInstanceOf[YNodePlain].value.asInstanceOf[YScalar].text
      name
    })
    seen ++= toAdd
    do {
      val nextItem = toAdd.dequeue()
      val yNode = typeMap(nextItem)
      addToDeclarations(yNode, (toAdd,seen));
    } while (!toAdd.isEmpty)
    ctx.declarations.futureDeclarations.resolve()
    val endPoints = starters.flatMap(n => {
      val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
      val path = currNode.get("url").get.value.asInstanceOf[YScalar].text
      val name = currNode.get("name").get.value.asInstanceOf[YScalar].text
      val postBodyParameter = Payload().withMediaType("application/json")
      val bodObj = ctx.declarations.shapes(name)

      postBodyParameter.withSchema(bodObj.link(name) /* .asInstanceOf[NodeShape].withName(name) */)
      val postRequest = Request().withPayloads(Seq(postBodyParameter))
      val outShape = ctx.declarations.shapes("PostResponse")
      val postResponse = Response().withStatusCode("201").withPayloads(Seq(Payload().withMediaType("application/json").
        withSchema(outShape.link("PostResponse").asInstanceOf[NodeShape].withName("PostResponse"))))
      val postOperation = Operation().withMethod("post").withRequest(postRequest).withResponses(Seq(postResponse))
      // val patchOperation = Operation().withMethod("patch")

      val uriParameter = Seq(Parameter().withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))
      val getOperation = Operation().withMethod("get").withResponses(Seq(Response()
        .withPayloads(Seq(postBodyParameter))
        .withStatusCode("200")))
        .withRequest(Request()
          .withUriParameters(Seq(Parameter().withBinding("path").withRequired(true)
            .withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))))
          .withQueryParameters(Seq(Parameter().withRequired(false).withName("fields").withBinding("query")
            .withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))))
      val patchOperation = Operation().withMethod("patch").withRequest(Request().withPayloads(Seq(postBodyParameter))
        .withUriParameters(Seq(Parameter().withBinding("path").withRequired(true)
          .withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))))
        .withResponses(Seq(Response().withStatusCode("201")))
      val deleteOperation = Operation().withMethod("delete").withResponses(Seq(Response().withStatusCode("204")))
        .withRequest(Request().withUriParameters(Seq(Parameter().withBinding("path").withRequired(true)
          .withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))))
      val endPoint = EndPoint().withPath(path.substring(path.lastIndexOf('/'))). // withParameters(uriParameter).
        withOperations(List(postOperation))
      val getPoint = EndPoint().withPath(path.substring(path.lastIndexOf('/')) + "/{ObjId}").withParameters(uriParameter).
        withOperations(List(getOperation,deleteOperation,patchOperation))
      List(endPoint, getPoint)
    })
    webapi.withEndPoints(endPoints)
    ctx.declarations.futureDeclarations.resolve()
    doc.withDeclares(
      ctx.declarations.shapes.values.toList ++
        ctx.declarations.annotations.values.toList
    )
  }

  private def parseWebAPI(baseUrl : String): Unit = {
    val webApi = WebApi()
    webApi.withName(root.location.split("/").last).withDefaultServer("https://mulesoft-30d-dev-ed.my.salesforce.com" + baseUrl)
    doc.adopted(root.location).withLocation(root.location).withEncodes(webApi)
  }
}
