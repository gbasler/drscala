object SimplifyIf {
  val a = Seq(1, 2, 3)

  val c = if (a.isEmpty) true else false

  val d = if (a.isEmpty) 1 else 2
}
