package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{OasLikeShapeEmitterContext, ShapeEmitterContext}

abstract class OasTypePartCollector(shape: Shape,
                                    ordering: SpecOrdering,
                                    ignored: Seq[Field],
                                    references: Seq[BaseUnit])(implicit spec: OasLikeShapeEmitterContext) {
  private var _emitters: Option[Seq[Emitter]]                          = None
  private var _emitter: Option[Either[PartEmitter, Seq[EntryEmitter]]] = None

  protected def getEmitters: Seq[Emitter] = _emitters.getOrElse(Nil)

  protected def emitters(pointer: Seq[String], schemaPath: Seq[(String, String)]): Seq[Emitter] = {
    _emitters match {
      case Some(ems) => ems
      case _ =>
        _emitters = Some(ordering.sorted(spec.typeEmitters(shape, ordering, ignored, references, pointer, schemaPath)))
        _emitters.get
    }
  }

  protected def emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitter(Nil, Nil)

  protected def emitter(pointer: Seq[String],
                        schemaPath: Seq[(String, String)]): Either[PartEmitter, Seq[EntryEmitter]] = _emitter match {
    case Some(em) => em
    case _ =>
      _emitter = Some(
        emitters(pointer, schemaPath) match {
          case Seq(p: PartEmitter)                           => Left(p)
          case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
          case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
        }
      )
      _emitter.get
  }
}
