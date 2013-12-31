package drscala
package doctors

import com.typesafe.config.{ConfigFactory, Config}

sealed abstract class ConfigType

object ConfigType {

  case object Ignore extends ConfigType

  case object Warn extends ConfigType

  val allTypes = Set(Ignore, Warn)

  val fromString: Map[String, ConfigType] = allTypes.map(c => c.getClass.getSimpleName -> c).toMap

}

// we have a constructor allowing the app to provide a custom Config
class Context(config: Config) {

  implicit def configFromString(configType: String) = ConfigType.fromString.get(configType).getOrElse(
    sys.error(s"Invalid config type [$configType].")
  )

  // This verifies that the Config is sane and has our
  // reference config. Importantly, we specify the "simple-lib"
  // path so we only validate settings that belong to this
  // library. Otherwise, we might throw mistaken errors about
  // settings we know nothing about.
  private val prefix = "drscala"

  config.checkValid(ConfigFactory.defaultReference(), prefix)

  // This uses the standard default Config, if none is provided,
  // which simplifies apps willing to use the defaults
  def this() {
    // don't use Thread.currentThread().getContextClassLoader()
    // since this classloader does not contain the plugin jar
    this(ConfigFactory.load(classOf[Context].getClassLoader))
  }

  val classloader = this.getClass.getClassLoader
  //Thread.currentThread().getContextClassLoader()
  val is = classloader.getResourceAsStream("reference.conf")
  val c = scala.io.Source.fromInputStream(is).getLines().mkString
  println(c)

  println(config)

  val simplifyIf: ConfigType = config.getString(prefix + ".simplify-if")


}
