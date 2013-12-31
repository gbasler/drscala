object MapGetOrElse {
  val y: Option[Int] = Some(3)
  val x = y.map(c => c % 3 == 0).getOrElse(false)
}
