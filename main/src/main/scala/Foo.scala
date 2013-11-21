package foo

object Bar {
  //  def oget[T](o: Option[T]) = o.get

  //  def mget[K, V](o: Map[K, V], k: K, d: V) = o.get(k).getOrElse(d)

  def mapfind[K, V](o: Map[K, V], k: K) = o.find(_._1 == k)

  def listfind[K](o: List[K], k: K) = o.find(_ == k)

  def NoType = "hello"

//  map(_ == XmlBasketType.WEIGHTED_AVERAGE)


  // if (riskyDiscountCurveName.isDefined) Some(YieldCurveKey(currency, riskyDiscountCurveName)) else None

  // if (factorCorrelationProduct <= Epsilon.Tiny || (abs(sTRho) >= sTToLTFactorWeightRatio * abs(lTRho))) true
  //   else false

  // require(size == errorWeights.rows,  sys.error(s"Num rows $size in targe")


}











