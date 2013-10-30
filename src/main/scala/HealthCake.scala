package drscala

import scala.tools.nsc.{Global, Phase}

// Note: Dear Cake, I hate you.
trait HealthCake {
  val global: Global

  import global._

  type PhaseId = String
  type Message = String

  sealed abstract class Doctor {
    def name: String
    def diagnostic: PartialFunction[PhaseId, CompilationUnit => Seq[(Position, String)]]
  }

  object Doctor {
    class StdLib extends Doctor {
      def name = "std-lib"

      val unsafeOnEmptyIterable = Seq("head", "last", "reduce", "reduceLeft", "reduceRight")

      def isNothingInferred(tree: Tree) = PartialFunction.cond(tree) {
        case ValDef(_, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
        case DefDef(_, _, _, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
      }

      val diagnostic: PartialFunction[PhaseId, CompilationUnit => Seq[(Position, Message)]] = {
        case "parser" => _.body.collect {
          case tree@Ident(name) if name.toString == "$qmark$qmark$qmark" => 
            tree.pos -> "Oops, an implementation is missing here."

          case tree@Apply(Ident(name), _) if name.toString == "println" => 
            tree.pos -> "There is rarely a good reason to use `println`, is it the case here?"

          case tree@Select(_, name) if name.toString == "asInstanceOf" => 
            tree.pos -> "There should be a better way than using `asInstanceOf`, what do you think?"
        }
        case "typer" => _.body.collect {
          case tree if isNothingInferred(tree) =>
            tree.pos -> "I feel a disturbance in the force, the type `Nothing` might have been inferred."

          case tree@Select(value, name) if unsafeOnEmptyIterable.contains(name.toString) && value.tpe <:< typeOf[Iterable[Any]] => 
            tree.pos -> (
              s"Are you sure the `${value.tpe.typeSymbol.name}` will never be empty?\n" +
              s"Because calling `$name` might throw an exception in this case."
            )

          case tree@Select(value, name) if name.toString == "get" && value.tpe <:< typeOf[Option[Any]] => 
            tree.pos -> "There is surely a better way than calling `Option.get`, any idea?"
        }
      }
    }
  }
}

