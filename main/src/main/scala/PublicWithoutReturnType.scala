trait Base {
  def doit(dummy: Int): Int

  def someSideeffect()

  val fixed: Double
}

object PublicWithoutReturnType extends Base {
  // warn about this
  def NoType = "hello"

  // ok, since private
  private def NoTypePriv = "hello"

  // be ok with that since type annotation
  def WithType: String = "hello"

  // ok, since type defined in Base
  def doit(dummy: Int) = 4

  // ok, only sideeffect
  def someSideeffect() = println("hello")

  // not ok, since not defined in Base (even though a function with this name is defined)
  def doit = 4.0

  val fixed = 4.0

  def onlySideEffect = {
    println("sideeffect")
  }
}
