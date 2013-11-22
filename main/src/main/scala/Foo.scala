package foo

sealed abstract class Ni {
  def a: Int
}

case class A(a: Int, b: Array[Int], private val c: Int, d: Array[Double]) extends Ni

object Bar {
  //  def oget[T](o: Option[T]) = o.get

  //  def mget[K, V](o: Map[K, V], k: K, d: V) = o.get(k).getOrElse(d)

  def mapfind[K, V](o: Map[K, V], k: K) = o.find(_._1 == k)

  def listfind[K](o: List[K], k: K) = o.find(_ == k)

  def NoType = "hello"

  // better a.indices
  val a = Seq(1, 2, 3)
  for (i <- 0 until a.size) {
    println(i)
  }


//  map(_ == XmlBasketType.WEIGHTED_AVERAGE)


  // if (riskyDiscountCurveName.isDefined) Some(YieldCurveKey(currency, riskyDiscountCurveName)) else None

  // if (factorCorrelationProduct <= Epsilon.Tiny || (abs(sTRho) >= sTToLTFactorWeightRatio * abs(lTRho))) true
  //   else false

  // require(size == errorWeights.rows,  sys.error(s"Num rows $size in targe")


}











