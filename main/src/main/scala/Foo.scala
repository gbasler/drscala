package foo

object Bar {
  //  def oget[T](o: Option[T]) = o.get

  //  def mget[K, V](o: Map[K, V], k: K, d: V) = o.get(k).getOrElse(d)

  def mapfind[K, V](o: Map[K, V], k: K) = o.find(_._1 == k)

  def listfind[K](o: List[K], k: K) = o.find(_ == k)

  def NoType = "hello"

//  map(_ == XmlBasketType.WEIGHTED_AVERAGE)


  // call skipped in specs2 test
}











