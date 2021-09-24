package amf.sfdc.internal.spec.document

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain._
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.SearchScope
import amf.sfdc.internal.spec.context.SfdcWebApiContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnresolvedShape}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.YNodeLike.toString
import org.yaml.model._


case class SfdcDocumentParser(root: Root)(implicit val ctx: SfdcWebApiContext) {

  val doc: Document = Document()
  def webapi: WebApi = doc.encodes.asInstanceOf[WebApi]

  protected def parseObjectRange(n: YNode, literalReference: String)(implicit ctx: SfdcWebApiContext): AnyShape = {
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
        shape.withContext(ctx)
        shape.unresolved(literalReference, Seq(), Some(new SourceLocation(n.location.sourceName, 0, 0,
          n.location.lineFrom, n.location.columnFrom, n.location.lineTo, n.location.columnTo)))
        shape
    }
  }

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[SyamlParsedDocument].document.node; //root.parsed.asInstanceOf[SyamlParsedDocument].document
    val preBase = ast.value.asInstanceOf[YSequence].nodes(0).value.asInstanceOf[YMap].map.get("url").get.value.asInstanceOf[YScalar].text
    val baseUrl = preBase.substring(0, preBase.lastIndexOf('/') + 1)
    parseWebAPI(baseUrl)
    ctx.declarations += NodeShape().add(DeclaredElement()).withId("http://salesforce.com/PostResponse").withName("PostResponse").withProperties(
      Seq(PropertyShape().withName("id").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")),
        // PropertyShape().withName("errors").withName(ArrayShape().withLinkTarget()),
        PropertyShape().withName("success").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#boolean"))))

    ast.children(0).children.map(n => {
      val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
      val path = currNode.get("url").get.value.toString
      val name = currNode.get("name").get
      val preschema = currNode.get("structureInfo").get.value.asInstanceOf[YMap].map.get("fields").get.value.asInstanceOf[YSequence].nodes
      val schema = preschema
        .foldLeft(Seq[PropertyShape]())((props, field) => {
          val fmap = field.value.asInstanceOf[YMap].map

          val required = fmap.get("nillable").get.value.toString
          val multiple = fmap.get("unique").get.value.toString
          val prename = fmap.get("name").get.value.toString
          val propName = prename.substring(1,prename.length - 1)
          val pretyper = fmap.get("type").get.value.toString
          val typer = pretyper.substring(1, pretyper.length - 1)
          typer match {
            case "id" | "picklist" | "string" | "textarea" | "url" | "phone" | "address" | "email" | "anyType" |
                 "currency" | "complexvalue" | "json" | "encryptedstring" | "combobox" | "base64" | "multipicklist" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
            }
            case "boolean" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#boolean"))
            }
            case "datetime" | "date" | "time" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#dateTime"))
            }
            case "double" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#double"))
            }
            case "int" | "long" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#integer"))
            }
            case "percent" => {
              props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#decimal"))
            }
            case "reference" => {
              try {
                fmap.get("referenceTo") match {
                  case Some(ynode) => {
                    val refName = ynode.value.asInstanceOf[YSequence].nodes(0).value.asInstanceOf[YScalar].text
                    val unresolved = parseObjectRange(field, refName)
                    props :+ PropertyShape().withName(propName).withRange(unresolved)
                  }
                  case None =>
                    props :+ PropertyShape().withName(propName).withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
                }
              } catch {
                case e : Exception => {
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
      val postSchema = NodeShape().withId("http://salesforce.com/" + name.value.asInstanceOf[YScalar].text).withName(name).withProperties(schema)

      ctx.declarations += postSchema.add(DeclaredElement())
    })
    ctx.declarations.futureDeclarations.resolve()
    val endPoints = ast.children(0).children.take(2).flatMap(n => {
      val currNode = n.asInstanceOf[YNodePlain].value.asInstanceOf[YMap].map
      val path = currNode.get("url").get.value.asInstanceOf[YScalar].text
      val name = currNode.get("name").get.value.asInstanceOf[YScalar].text
      val postBodyParameter = Payload().withMediaType("application/json")
      val bodObj = ctx.declarations.shapes(name)


      val aShape = NodeShape().withId("http://salesforce.com/foo" + name).withName("foo" + name).withProperties(Seq(
        PropertyShape().withName("burp").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")),
          PropertyShape().withName("gorp").withRange(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
        , PropertyShape().withName("bingo").withRange(ctx.declarations.shapes(name).link(name) /* .asInstanceOf[NodeShape].withName(name) */ )
      ))

      postBodyParameter.withSchema(aShape /* .link(name).asInstanceOf[NodeShape].withName(name) */)
      val postRequest = Request().withPayloads(Seq(postBodyParameter))
      val outShape = ctx.declarations.shapes("PostResponse")
      val postResponse = Response().withStatusCode("200").withPayloads(Seq(Payload().withMediaType("application/json").
        withSchema(outShape.link("PostResponse").asInstanceOf[NodeShape].withName("PostResponse"))))
      val postOperation = Operation().withMethod("post").withRequest(postRequest).withResponses(Seq(postResponse))
      // val patchOperation = Operation().withMethod("patch")

      val uriParameter = Seq(Parameter().withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))
      val getOperation = Operation().withMethod("get").withResponses(Seq(Response()
        .withPayloads(Seq(postBodyParameter))
        // .withPayloads(Seq(Payload().withMediaType("text/html").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))))
        .withStatusCode("200")))
        // .withRequest(Request().withUriParameters(Seq(Parameter().withName("ObjId").withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")))))
      val endPoint = EndPoint().withPath(path.substring(path.lastIndexOf('/'))). // withParameters(uriParameter).
        withOperations(List(getOperation))
      val getPoint = EndPoint().withPath(path.substring(path.lastIndexOf('/')) + "{ObjId}").
        withOperations(List(getOperation))
      List(endPoint)
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
    webApi.withName(root.location.split("/").last).withDefaultServer("https://salesforce.com/" + baseUrl)
    doc.adopted(root.location).withLocation(root.location).withEncodes(webApi)
  }
}
