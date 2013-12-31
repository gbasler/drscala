object IsDefinedGet {
  val mean = true // don't let mean fool you!
  val o: Option[Int] = Some(1)
  // one of the most awesome crap patterns:
  if(o.isDefined && mean) {
    val v = o.get
  }

  // alright... we don't want false positives...
  val a = o.get

  // should ignore similar constructs without Option
  abstract class Oops {
     def isDefined: Boolean = false
     def get: Int = 0
  }

  var falseFriend: Oops = _
  if(falseFriend.isDefined && mean) {
    val v = falseFriend.get
  }
}
