object Find {
  def oget[T](o: Option[T]) = o.get

  def mget[K, V](o: Map[K, V], k: K, d: V) = o.get(k).getOrElse(d)

  def setfind[V](o: Set[V], v: V) = o.find(_ == v)

  def mapfind[K, V](o: Map[K, V], k: K) = o.find(_._1 == k)

  def listfind[K](o: List[K], k: K) = o.find(_ == k)

}
