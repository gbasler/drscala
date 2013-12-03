trait Base {
  def doit: Int
}

object PublicWithoutReturnType extends Base {
  // warn about this
  def NoType = "hello"

  private def NoTypePriv = "hello"

  // be ok with that
  def WithType: String = "hello"


  // ok, since type defined in Base
  def doit = 4

  def onlySideEffect = {
    println("sideeffect")
  }
}
