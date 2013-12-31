object IsDefinedGet {
  val mean = true // don't let mean fool you!
  val o: Option[Int] = Some(1)
  // one of the most awesome crap patterns:
  if(o.isDefined && mean) {
    val v = o.get
  }

  // alright... we don't want false positives...
  val a = o.get
}
