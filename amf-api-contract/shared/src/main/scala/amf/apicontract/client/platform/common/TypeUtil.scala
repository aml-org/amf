package amf.apicontract.client.platform.common

import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.security.{
  OAuth1SettingsModel,
  OAuth2SettingsModel,
  SecuritySchemeModel
}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, RequestModel, ResponseModel, ServerModel}
import amf.core.client.platform.model.AmfObjectWrapper
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.metamodel.Type
import amf.core.internal.metamodel.document.{BaseUnitModel, DocumentModel}
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.core.internal.metamodel.domain._
import amf.shapes.internal.domain.metamodel._
import amf.apicontract.client.scala.common.{TypeUtil => InternalTypeUtil}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("TypeUtil")
@JSExportAll
object TypeUtil {

  /** Checks if some AmfObject is of an specific type
    *
    * @param element
    *   AmfObject to validate the type
    * @param typeIri
    *   IRI of the type wanted to validate. It could be one of the IRIs defined in [[TypeIRI]] or other
    * @return
    *   a boolean value indicating if the AmfElement is of that type (true if it is, false if not)
    */
  def isTypeOf(element: AmfObjectWrapper, typeIri: String): Boolean = InternalTypeUtil.isTypeOf(element, typeIri)

  /** Checks if some AmfObject is of at least one type of a type list
    *
    * @param element
    *   AmfObject to validate the type
    * @param typeIris
    *   list of IRIs of the types wanted to validate. They could be some of the IRIs defined in [[TypeIRI]] or other
    * @return
    *   a boolean value indicating if the AmfElement is of at least one type of the list (true if it is, false if not)
    */
  def isTypeOf(element: AmfObject, typeIris: Seq[String]): Boolean = {
    typeIris.exists(typeIri => isTypeOf(element, typeIri))
  }

}

@JSExportTopLevel("TypeIRI")
@JSExportAll
object TypeIRI {
  val Shape: String                = getTypeIri(ShapeModel)
  val RecursiveShape: String       = getTypeIri(RecursiveShapeModel)
  val PropertyShape: String        = getTypeIri(PropertyShapeModel)
  val AnyShape: String             = getTypeIri(AnyShapeModel)
  val NodeShape: String            = getTypeIri(NodeShapeModel)
  val ArrayShape: String           = getTypeIri(ArrayShapeModel)
  val ScalarShape: String          = getTypeIri(ScalarShapeModel)
  val NilShape: String             = getTypeIri(NilShapeModel)
  val FileShape: String            = getTypeIri(FileShapeModel)
  val SchemaShape: String          = getTypeIri(SchemaShapeModel)
  val UnionShape: String           = getTypeIri(UnionShapeModel)
  val MatrixShape: String          = getTypeIri(MatrixShapeModel)
  val TupleShape: String           = getTypeIri(TupleShapeModel)
  val Document: String             = getTypeIri(DocumentModel)
  val BaseUnit: String             = getTypeIri(BaseUnitModel)
  val SecurityScheme: String       = getTypeIri(SecuritySchemeModel)
  val Request: String              = getTypeIri(RequestModel)
  val Response: String             = getTypeIri(ResponseModel)
  val OAuth1Settings: String       = getTypeIri(OAuth1SettingsModel)
  val OAuth2Settings: String       = getTypeIri(OAuth2SettingsModel)
  val EndPoint: String             = getTypeIri(EndPointModel)
  val Server: String               = getTypeIri(ServerModel)
  val CustomDomainProperty: String = getTypeIri(CustomDomainPropertyModel)
  val ScalarNode: String           = getTypeIri(ScalarNodeModel)
  val ObjectNode: String           = getTypeIri(ObjectNodeModel)
  val ArrayNode: String            = getTypeIri(ArrayNodeModel)
  val WebApi: String               = getTypeIri(WebApiModel)

  private def getTypeIri(model: Type) = {
    model.`type`.head.iri()
  }
}
