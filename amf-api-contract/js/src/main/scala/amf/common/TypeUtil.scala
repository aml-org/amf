package amf.common

import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.metamodel.Type
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.metamodel.domain.{RecursiveShapeModel, ShapeModel}
import amf.shapes.internal.domain.metamodel._

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
  def isTypeOf(element: AmfObject, typeIri: String): Boolean = {
    element.meta.`type`.map(_.iri()).contains(typeIri)
  }

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
  val Shape: String          = getTypeIri(ShapeModel)
  val RecursiveShape: String = getTypeIri(RecursiveShapeModel)
  val PropertyShape: String  = getTypeIri(PropertyShapeModel)
  val AnyShape: String       = getTypeIri(AnyShapeModel)
  val NodeShape: String      = getTypeIri(NodeShapeModel)
  val ArrayShape: String     = getTypeIri(ArrayShapeModel)
  val ScalarShape: String    = getTypeIri(ScalarShapeModel)
  val NilShape: String       = getTypeIri(NilShapeModel)
  val FileShape: String      = getTypeIri(FileShapeModel)
  val SchemaShape: String    = getTypeIri(SchemaShapeModel)
  val UnionShape: String     = getTypeIri(UnionShapeModel)
  val MatrixShape: String    = getTypeIri(MatrixShapeModel)
  val TupleShape: String     = getTypeIri(TupleShapeModel)

  private def getTypeIri(model: Type) = {
    model.`type`.head.iri()
  }
}
