package drscala
package doctors

trait StdLibComponent { self: HealthCake =>
  import self.global._

  class StdLib extends Doctor {
    def name = "std-lib"

    val unsafeOnEmptyIterable = Seq("head", "last", "reduce", "reduceLeft", "reduceRight")

    def isNothingInferred(tree: Tree) = PartialFunction.cond(tree) {
      case ValDef(_, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
      case DefDef(_, _, _, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
    }

    override val diagnostic: PartialFunction[PhaseId, CompilationUnit => Seq[(Position, Message)]] = {
      case "parser" => _.body.collect {
        case tree@Ident(name) if name.toString == "$qmark$qmark$qmark" =>
          tree -> "Oops, an implementation is missing here."

        case tree@Apply(Ident(name), _) if name.toString == "println" =>
          tree -> "There is rarely a good reason to use `println`, is it the case here?"

        case tree@Select(_, name) if name.toString == "asInstanceOf" =>
          tree -> "There should be a better way than using `asInstanceOf`, what do you think?"
      }
      case "typer" => _.body.collect {
        case tree if isNothingInferred(tree) =>
          tree -> "I feel a disturbance in the force, the type `Nothing` might have been inferred."

        case tree@Select(value, name) if unsafeOnEmptyIterable.contains(name.toString) && value.tpe <:< typeOf[Iterable[Any]] =>
          tree -> (
            s"Are you sure the `${value.tpe.typeSymbol.name}` will never be empty?\n" +
            s"Because calling `$name` might throw an exception in this case."
          )

        case tree@Select(value@Apply(Select(ident, n1), _), n2)
          if n1.toString == "get" && n2.toString == "getOrElse" && value.tpe <:< typeOf[Option[Any]] =>
          tree -> s"`$ident.get.getOrElse` are you serious? Try `$ident.getOrElse` instead."

        case tree@Select(value, name) if name.toString == "find" && value.tpe.baseClasses.exists(_.tpe.toString().contains("scala.collection.Map")) =>
          tree -> s"`find` on a `Map`, you are joking???"

        case tree@Select(value, name) if name.toString == "find" && value.tpe.baseClasses.exists(_.tpe.toString().contains("scala.collection.Set")) =>
          tree -> s"`find` on a `Set`, you are joking???"

        case tree@Select(v@Apply(value@Select(ident, n1), a1), n2) if n1.toString == "get" && n2.toString == "getOrElse" =>
          println(ident.tpe <:< typeOf[scala.collection.Map[Any, Any]])
          println(ident.tpe <:< typeOf[scala.Predef.Map[Any, Any]])
          println(show(ident.tpe))
        import BooleanFlag._
          println(showRaw(ident.tpe, printTypes = true))
          println(showRaw(value.tpe, printTypes = true))
          println(showRaw(tree.tpe, printTypes = true))
          println(show(ident))
          println(showRaw(ident))
          println(show(tree))
          println("!"+showRaw(tree,printTypes = true))
        println( tree.tpe <:< typeOf[Option[Any]])
        println( v.tpe <:< typeOf[Option[Any]])
          tree -> s"wtf $n1 $a1 $n2 $ident ${ident.tpe}"

        case tree@Select(value, name) if name.toString == "get" /*&& value.tpe <:< typeOf[scala.collection.Map[Any, Any]]*/ =>
          tree -> s"`Map.get`, any idea? ${value.summaryString} ${value.tpe.toString()}"
      }
    }

    override val examine: Seq[(String, Column => Position)] => Seq[(Position, Message)] = xs => {
      def emptyLines(lines: Seq[(String, Column => Position)]): Seq[(Position, Message)] = {
        val (count, xs) = lines.foldLeft((0, Seq.empty[Position])) { case ((count, xs), (line, pos)) =>
          if (line.trim.isEmpty) (count + 1) ->  xs
          else 0 -> { if (count > 1) (pos(1) +: xs ) else xs }
        }
        xs.map { case (line, column) => (line - 1, column) -> "Are these extra empty lines really needed?" }
      }

      xs.collect {
        case (code, pos) if code.trim.endsWith(";") =>
          pos(code.length) -> "That `;` at the end of the line is unnecessary."
      } ++ emptyLines(xs)
    }
  }
}
