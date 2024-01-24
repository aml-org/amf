package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.scala.model.document.FieldsFilter
import amf.core.client.scala.model.domain.AmfElement
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.parser.domain.{FieldEntry, Fields}

/** Scope does not include external references (like FieldsFilter.Local) and also removes endpoints to avoid validating
  * them twice in graphql (because they are also parsed as types)
  */
object GraphQLFieldsFilter extends FieldsFilter {

  override def filter(fields: Fields): List[AmfElement] =
    fields
      .fields()
      .filter(_.field != DocumentModel.References) // remove external refs
      .filter(_.field != WebApiModel.EndPoints)    // remove endpoints
      .map(_.element)
      .toList
}
