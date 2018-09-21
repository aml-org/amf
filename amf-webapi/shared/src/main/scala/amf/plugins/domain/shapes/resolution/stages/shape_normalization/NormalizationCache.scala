package amf.plugins.domain.shapes.resolution.stages.shape_normalization
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.parser.ErrorHandler
import amf.plugins.features.validation.ParserSideValidations
import amf.{ProfileName, Raml08Profile}

import scala.collection.mutable

private[plugins] class NormalizationContext(final val errorHandler: ErrorHandler,
                                            final val keepEditingInfo: Boolean,
                                            final val profile: ProfileName,
                                            val cache: NormalizationCache = NormalizationCache()) {

  val isRaml08: Boolean                        = profile.equals(Raml08Profile)
  private val minShapeClass: MinShapeAlgorithm = new MinShapeAlgorithm()(this)

  def minShape(derivedShape: Shape, superShape: Shape): Shape =
    try {
      minShapeClass.computeMinShape(derivedShape, superShape)
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        errorHandler.violation(
          ParserSideValidations.InvalidTypeInheritanceWarningSpecification.id,
          derivedShape.id,
          e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
          e.getMessage,
          e.lexicalInfo,
          None
        )
        derivedShape
      case other: Exception => throw other
    }

}

private[shape_normalization] case class NormalizationCache() {
  def updateFixPointsAndClojures(canonical: Shape): Unit = {
    updateRecursiveTargets(canonical)
    cache.values.foreach { s =>
      s.closureShapes.find(clo => clo.id == canonical.id && clo != canonical) match {
        case Some(clo) =>
          s.closureShapes.remove(clo)
          s.closureShapes += canonical
        case _ => // ignore
      }
    }
  }

  private def updateRecursiveTargets(newShape: Shape): NormalizationCache = {
    fixPointCache.values.flatten
      .filter(_.fixpointTarget.exists(_.id == newShape.id))
      .foreach(_.fixpointTarget = Some(newShape))
    this
  }

  def removeIfPresent(shape: Shape): this.type = {
    get(shape.id) match {
      case Some(s) if s.equals(shape) => cache.remove(shape.id)
      case _                          =>
    }
    this
  }

  def registerMapping(id: String, alias: String): this.type = {
    mappings.get(alias) match {
      case Some(a) =>
        mappings.remove(alias)
        mappings.put(id, a)
      case _ =>
        mappings.put(id, alias)
        fixPointCache.get(id).foreach { seq =>
          fixPointCache.remove(id)
          fixPointCache.put(alias, seq.map(_.withFixPoint(alias)))
        }
    }
    this
  }

  private val cache         = mutable.Map[String, Shape]()
  private val fixPointCache = mutable.Map[String, Seq[RecursiveShape]]()
  private val mappings      = mutable.Map[String, String]()

  private def registerFixPoint(r: RecursiveShape): RecursiveShape = {
    r.fixpoint.option().foreach { fp =>
      val alias = mappings.get(fp)
      fixPointCache.get(fp) match {
        case Some(s) =>
          val shapes = s :+ r
          val newAlias = alias.fold({
            fp
          })(a => {
            shapes.foreach(_.withFixPoint(a))
            fixPointCache.remove(fp)
            a
          })
          fixPointCache.put(newAlias, shapes)

        case _ =>
          alias.fold({
            fixPointCache.put(fp, Seq(r))
          })(a => {
            r.withFixPoint(a)
            fixPointCache.put(a, Seq(r))
          })
      }
    }
    r
  }

  def +(shape: Shape): this.type = {
    shape match {
      case r: RecursiveShape =>
        registerFixPoint(r)
      case _ =>
    }
    cache.put(shape.id, shape)
    this
  }

  def get(id: String): Option[Shape] = cache.get(id)

  def exists(id: String): Boolean = cache.contains(id)
}
