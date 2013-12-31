package drscala
package doctors

import com.typesafe.config.{ConfigFactory, Config}
import drscala.doctors.ConfigType.{Warn, Ignore}

sealed abstract class ConfigType {
  def isWarning: Boolean = this match {
    case Warn => true
    case _ => false
  }
}

object ConfigType {

  case object Ignore extends ConfigType

  case object Warn extends ConfigType

  val allTypes = Set(Ignore, Warn)

  val fromString: Map[String, ConfigType] = allTypes.map(c => c.toString.toLowerCase -> c).toMap

}

private object ClassLoaderWorkaround {
  // don't use Thread.currentThread().getContextClassLoader()
  // since this classloader does not contain the plugin jar
  val classLoader = classOf[Context].getClassLoader
}

// we have a constructor allowing the app to provide a custom Config
class Context(config: Config) {

  implicit def configFromString(configType: String) = ConfigType.fromString.get(configType.toLowerCase).getOrElse(
    sys.error( s"""Invalid config type [$configType]. Allowed types are ${ConfigType.fromString.keys.mkString(",")}.""")
  )

  private val prefix = "drscala"


  // This verifies that the Config is sane and has our
  // reference config. Importantly, we specify the prefix
  // path so we only validate settings that belong to this
  // library. Otherwise, we might throw mistaken errors about
  // settings we know nothing about.
  config.checkValid(ConfigFactory.defaultReference(ClassLoaderWorkaround.classLoader), prefix)

  // This uses the standard default Config, if none is provided,
  // which simplifies apps willing to use the defaults
  def this() {
    this(ConfigFactory.load(ClassLoaderWorkaround.classLoader))
  }

  val simplifyIf: ConfigType = config.getString(prefix + ".simplify-if")
  val nothingInferred: ConfigType = config.getString(prefix + ".nothing-inferred")
  val isDefinedGet: ConfigType = config.getString(prefix + ".isDefined-get")
  val caseClassWithArray: ConfigType = config.getString(prefix + ".case-class-with-array")
  val findOnSet: ConfigType = config.getString(prefix + ".find-on-set")
  val findOnMap: ConfigType = config.getString(prefix + ".find-on-map")
  val azInstanceOf: ConfigType = config.getString(prefix + ".asInstanceOf")
  val getGetOrElse: ConfigType = config.getString(prefix + ".get-getOrElse")
  val mapGetOrElse: ConfigType = config.getString(prefix + ".map-getOrElse")
  val unsafeOnEmptyIterable: ConfigType = config.getString(prefix + ".unsafe-on-empty-iterable")
  val println: ConfigType = config.getString(prefix + ".println")
  val missingImplementation: ConfigType = config.getString(prefix + ".missing-implementation")
  val checkNoReturnType: ConfigType = config.getString(prefix + ".check-no-return-type")
  val emptyLines: ConfigType = config.getString(prefix + ".empty-lines ")

  override def toString: String = {
    val configs: Seq[(String, ConfigType)] = Seq(
      "simplify if" -> simplifyIf,
      "nothing inferred" -> nothingInferred,
      "isDefined get" -> isDefinedGet,
      "case class with array" -> caseClassWithArray,
      "find on set" -> findOnSet,
      "find on map" -> findOnMap,
      "asInstanceOf" -> azInstanceOf,
      "get(...).getOrElse" -> getGetOrElse,
      "map(...).getOrElse" -> mapGetOrElse,
      "unsafe on empty iterable" -> unsafeOnEmptyIterable,
      "println" -> println,
      "missing implementation" -> missingImplementation,
      "check no return type" -> checkNoReturnType,
      "empty lines " -> emptyLines
    )
    val ordered = configs.sortBy(_._1)
    val longest = configs.map(_._1.length).max
    val content = for {
      (key, value) <- ordered
    } yield {
      s"""$key${" " * (longest - key.length)}: $value"""
    }
    s"""[Context]\n${content.mkString("\n")}"""
  }
}
