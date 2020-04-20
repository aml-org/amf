package amf.plugins.domain.shapes.resolution.stages.shape_normalization
import amf.core.model.domain.{RecursiveShape, Shape}

import scala.collection.mutable

private[shape_normalization] case class NormalizationCache() extends ClosureHelper {

  private val cache = mutable.Map[String, Shape]()
  /* Shape in the closure -> Shape that in s.closures contains the shape */
  private val cacheWithClosures = mutable.Map[String, Seq[Shape]]()

  private val fixPointCache = mutable.Map[String, Seq[RecursiveShape]]()
  private val mappings      = mutable.Map[String, String]()

  def cacheClosure(id: String, array: Shape): this.type = {
    cacheWithClosures.get(id) match {
      case Some(seq) => cacheWithClosures.update(id, seq :+ array)
      case _         => cacheWithClosures.update(id, Seq(array))
    }
    this
  }

  def cacheWithClosures(id: String): Option[Seq[Shape]] = cacheWithClosures.get(id)

  def updateFixPointsAndClosures(canonical: Shape, withoutCaching: Boolean): Unit = {
    // First check if the shape has itself as closure or fixpoint target (because of it still not in the cache)
    updateClosure(canonical, _.id == canonical.id, canonical)

    canonical match {
      case r: RecursiveShape if r.fixpointTarget.isDefined && r.fixpointTarget.get.id == canonical.id =>
        r.withFixpointTarget(canonical)
      case _ => // Ignore
    }

    // Then if the flag of caching is enabled, check and update other shapes
    if (!withoutCaching) {
      updateRecursiveTargets(canonical)
      cacheWithClosures.get(canonical.id) match {
        case Some(seq) =>
          seq.foreach { s =>
            val predicate: Shape => Boolean = clo => clo.id == canonical.id && clo != canonical
            updateClosure(s, predicate, canonical)
          }
        case _ => // ignore
      }
    }
  }

  def updateRecursiveTargets(newShape: Shape): NormalizationCache = {
    fixPointCache.values.flatten
      .filter(_.fixpointTarget.exists(_.id == newShape.id))
      .foreach(_.withFixpointTarget(newShape))
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
