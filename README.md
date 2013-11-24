# DrScala

You need to review code on Github? You keep making the same comments over and over?

DrScala takes care of your reviews and checks basic flaws of style and correctness.
It's main purpose is educational, it is not a code checker nor a bug finder.

## Configurations

## Setup in SBT

    autoCompilerPlugins := true

    addCompilerPlugin("com.github.aloiscochard" %% "drscala" % "0.1.0")

    scalacOptions ++= Seq(
      "debug",
      "warn",
      "gh.user=aloiscochard",
      "gh.password=42",
      "gh.repository.owner=aloiscochard",
      "gh.repository.name=drscala"
    ).map("-P:drscala:" + _)

    libraryDependencies += "org.kohsuke" % "github-api" % "1.44" % Configurations.ScalaTool

## Checks

#### `asInstanceOf[...]`

* use a pattern match or `collect`.

#### Public methods must have return type

* public methods are API's. They should have explicit types. Also the IntelliJ Scala plugin struggles since
it needs to infer all types for tab-competion.

#### Case classes must not have Arrays as constructor arguments

* structural equality / hashing does not work since. Arrays do not implement these methods.

#### `Option.get`

* `None.get` results in `NoSuchElementException`, it's better to use `getOrElse(sys.error(...))` and have a custom error message.

#### `Map.find`, `Set.find`

* `find` is O(n) whereas `contains`/`get` run in O(log n).

#### `Map.get.getOrElse`

* can be simplified to `Map.getOrElse`.

#### `!empty`

* Scala collections offer `nonEmpty`.

#### `filter(!...)`

* write `filterNot(...)`.


#### Collection.`size == 0`

* write `isEmpty`.


## Code reviewing

The compiler plugin will read the env variables `DRSCALA_PR` or `ghprbPullId` as well as the system property `drscala.pr`,
to find the pull request ID that should be automatically reviewed.

## Jenkins - GitHub Pull Request Builder

DrScala can be easily integrated with the jenkins plugin that automate building of pull request.
The plugin will automatically detect the pull request to review using the env variable `ghprbPullId.`
It will only run if `-Ddrscala.enable=true` is specified
in the build configuration. The reason is that some checks are expensive and would slow down compilation times on the
local build.

## TODO

* Integrate [copy-paste detector](http://pmd.sourceforge.net/cpd.html).
* Integrate [test coverage tool](http://www.eclemma.org/jacoco/) and warn if a function is not covered by a test.

## Links

* [Boxer, a demonstration of how to setup a plugin in SBT](http://github.com/retronym/boxer).
* [Scala style checker](http://github.com/scalastyle/scalastyle)
* [Wartremover, compiler plugin that does some simple checks](https://github.com/puffnfresh/wartremover)