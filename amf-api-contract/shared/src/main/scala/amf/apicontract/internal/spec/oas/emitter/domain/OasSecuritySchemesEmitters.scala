package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasSpecEmitterContext
import amf.apicontract.internal.spec.oas.{OasLikeSecuritySchemeTypeMappings, OasSecuritySchemeType, SecuritySchemeType}
import amf.apicontract.internal.spec.raml.emitter.domain.Raml10DescribedByEmitter
import amf.apicontract.internal.spec.spec.toRaml
import amf.core.client.common.position.Position
import amf.core.internal.parser.domain.Fields
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.spec.common.emitter.ExternalReferenceUrlEmitter.handleInlinedRefOr
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

abstract class OasSecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val securityTypeMap: Seq[(SecuritySchemeType, SecurityScheme)] =
      securitySchemes.map(s => (OasLikeSecuritySchemeTypeMappings.mapsTo(spec.spec, s.`type`.value()), s))

    val (oasSecurityDefinitions, extensionDefinitions) = securityTypeMap.partition {
      case (OasSecuritySchemeType(_), _) => true
      case _                             => false
    }

    if (oasSecurityDefinitions.nonEmpty)
      b.entry(
        key,
        _.obj(
          traverse(
            ordering.sorted(oasSecurityDefinitions
              .map(s => emitter(s._2, s._1, ordering))),
            _
          ))
      )
    if (extensionDefinitions.nonEmpty)
      b.entry(
        "securitySchemes".asOasExtension,
        _.obj(traverse(ordering.sorted(extensionDefinitions.map(s => emitter(s._2, s._1, ordering))), _))
      )

  }

  override def position(): Position =
    securitySchemes.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)

  def key: String

  def emitter(securityScheme: SecurityScheme,
              mappedType: SecuritySchemeType,
              ordering: SpecOrdering): OasNamedSecuritySchemeEmitter
}

class Oas3SecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends OasSecuritySchemesEmitters(securitySchemes, ordering) {

  override def key = "securitySchemes"
  override def emitter(securityScheme: SecurityScheme, mappedType: SecuritySchemeType, ordering: SpecOrdering) =
    Oas3NamedSecuritySchemeEmitter(securityScheme, mappedType, ordering)
}

class Oas2SecuritySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends OasSecuritySchemesEmitters(securitySchemes, ordering) {

  override def key: String = "securityDefinitions"
  override def emitter(securityScheme: SecurityScheme, mappedType: SecuritySchemeType, ordering: SpecOrdering) =
    new OasNamedSecuritySchemeEmitter(securityScheme, mappedType, ordering)
}

class OasNamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                    mappedType: SecuritySchemeType,
                                    ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = pos(securityScheme.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = securityScheme.name.option() match {
      case Some(n) => n
      case None =>
        spec.eh.violation(
          TransformationValidation,
          securityScheme.id,
          None,
          s"Cannot declare security scheme without name $securityScheme",
          securityScheme.position(),
          securityScheme.location()
        )
        "default-"
    }

    b.entry(name, if (securityScheme.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    OasTagToReferenceEmitter(securityScheme).emit(b)
  }

  protected def emitInline(b: PartBuilder): Unit =
    new OasSecuritySchemeEmitter(securityScheme, mappedType, ordering).emit(b)

}

case class Oas3NamedSecuritySchemeEmitter(securityScheme: SecurityScheme,
                                          mappedType: SecuritySchemeType,
                                          ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext)
    extends OasNamedSecuritySchemeEmitter(securityScheme, mappedType, ordering) {

  override protected def emitInline(b: PartBuilder): Unit =
    Oas3SecuritySchemeEmitter(securityScheme, mappedType, ordering).emit(b)

}

class OasSecuritySchemeEmitter(securityScheme: SecurityScheme, mappedType: SecuritySchemeType, ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {

    val results = ListBuffer[EntryEmitter]()
    val fs      = securityScheme.fields

    fs.entry(SecuritySchemeModel.Type)
      .foreach(f => results += MapEntryEmitter("type", mappedType.text, position = pos(f.value.annotations)))
    fs.entry(SecuritySchemeModel.DisplayName).map(f => results += ValueEmitter("displayName".asOasExtension, f))
    fs.entry(SecuritySchemeModel.Description).map(f => results += ValueEmitter("description", f))

    results += Raml10DescribedByEmitter("describedBy".asOasExtension, securityScheme, ordering, Nil)(toRaml(spec))

    emitSettings(results, fs)

    ordering.sorted(results)
  }

  override def emit(b: PartBuilder): Unit = handleInlinedRefOr(b, securityScheme) {
    b.obj(traverse(ordering.sorted(emitters()), _))
  }

  override def position(): Position = pos(securityScheme.annotations)

  protected def emitSettings(results: ListBuffer[EntryEmitter], fs: Fields): Unit = {
    fs.entry(SecuritySchemeModel.Settings).foreach(f => results ++= OasSecuritySettingsEmitter(f, ordering).emitters())
  }
}

case class Oas3SecuritySchemeEmitter(securityScheme: SecurityScheme,
                                     mappedType: SecuritySchemeType,
                                     ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext)
    extends OasSecuritySchemeEmitter(securityScheme, mappedType, ordering) {
  val httpSchemaMappings = Map(
    "Basic Authentication"  -> "basic",
    "Digest Authentication" -> "digest"
  )

  private def isRamlHttpType(possibleType: String) = httpSchemaMappings.contains(possibleType)

  override protected def emitSettings(results: ListBuffer[EntryEmitter], fs: Fields): Unit = {
    val schemaType = securityScheme.`type`.value()
    if (isRamlHttpType(schemaType)) {
      securityScheme.withHttpSettings().withScheme(httpSchemaMappings.getOrElse(schemaType, ""))
    }
    super.emitSettings(results, fs)
  }
}
