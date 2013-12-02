package foo

//sealed abstract class Ni {
//  def a: Int
//}
//
//case class A(a: Int, b: Array[Int], private val c: Int, d: Array[Double]) extends Ni

object Bar {
  //  def oget[T](o: Option[T]) = o.get

  //  def mget[K, V](o: Map[K, V], k: K, d: V) = o.get(k).getOrElse(d)

  //  def mapfind[K, V](o: Map[K, V], k: K) = o.find(_._1 == k)
  //
  //  def listfind[K](o: List[K], k: K) = o.find(_ == k)
  //

  // warn about this
  def NoType = "hello"

  private def NoTypePriv = "hello"
  // be ok with that
  def WithType: String = "hello"

  def onlySideEffect = {
    println("sideeffect")
  }

  //
  //  // better a.indices
  //  val a = Seq(1, 2, 3)
  //  for (i <- 0 until a.size) {
  //    println(i)
  //  }
  //

  // simplifiable operation on collection
//  val y: Option[Int] = Some(1)
//  val x = y.map(c => true).getOrElse(false)
//
//  val shifts = Seq(None)
//  val b = !shifts.isEmpty

  //  map(_ == XmlBasketType.WEIGHTED_AVERAGE)

  // if (riskyDiscountCurveName.isDefined) Some(YieldCurveKey(currency, riskyDiscountCurveName)) else None

  // if (factorCorrelationProduct <= Epsilon.Tiny || (abs(sTRho) >= sTToLTFactorWeightRatio * abs(lTRho))) true
  //   else false

  // require(size == errorWeights.rows,  sys.error(s"Num rows $size in targe")

  // use o.collect...
//  val o: Option[Any] = Some(1)
//  val filtered = o match {
//    case value: Double if value > 1 => value
//  }

}











