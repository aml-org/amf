package amf.shapes.internal.spec.jsonldschema.parser.builder

object TupleOps {
  def reduce[T, R](tuples: List[(T, R)]): (List[T], List[R]) = {
    val start = (List.empty[T], List.empty[R])
    tuples.foldLeft(start) { (acc, curr) =>
      (acc._1 :+ curr._1, acc._2 :+ curr._2)
    }
  }
}
