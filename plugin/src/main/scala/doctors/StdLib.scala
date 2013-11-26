package drscala
package doctors

import scala.tools.nsc.Global

trait StdLibComponent {
  self: HealthCake =>

  import self.global._

  class StdLib extends Doctor {
    def name = "std-lib"

    val unsafeOnEmptyIterable = Seq("head", "last", "reduce", "reduceLeft", "reduceRight")

    def isNothingInferred(tree: Tree) = PartialFunction.cond(tree) {
      case ValDef(_, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
      case DefDef(_, _, _, _, tpt, _) if tpt.exists(_.tpe =:= typeOf[Nothing]) => true
    }

    // regular type check would not work because of covariance
    private def isMap(tpe: Type) = tpe.typeSymbol.isNonBottomSubClass(typeOf[Map[Any, Any]].typeSymbol)

    private def isSet(tpe: Type) = tpe.typeSymbol.isNonBottomSubClass(typeOf[Set[Any]].typeSymbol)

    private def isArray(tpe: Type) = tpe.typeSymbol.isNonBottomSubClass(typeOf[Array[Any]].typeSymbol)

    private def briefTree(tree: Tree) = {
      val t = show(tree)
      val line = t.indexOf('\n')
      if (line == -1) {
        t
      } else {
        t.substring(0, line - 1) + " ..."
      }
    }

    object CaseClassArrayMembersExtractor {
      def unapply(tree: Tree): Option[(TypeName, IndexedSeq[Symbol])] = tree match {
        case tree@ClassDef(mods, ident, _, _) if (tree.symbol ne null) && tree.symbol.isCaseClass =>
          val a = tree.symbol.tpe.declarations
            .filter(m => m.isCaseAccessor && isArray(m.tpe) && m.isParamAccessor && m.isMethod)
            .toIndexedSeq
          if (a.isEmpty) None else Some((ident, a))
        case _ => None
      }
    }

    override val diagnostic: PartialFunction[PhaseId, CompilationUnit => Seq[(Position, Message)]] = {
      case "parser" => _.body.collect {
        case tree@Select(_, name) if name.toString == "asInstanceOf" =>
          tree -> "An `asInstanceOf` could result in a `ClassCastException` at runtime, it's better to use a pattern match."
      }
      case "typer" =>
        cu =>
          println(cu)

          cu.body.collect {
            case tree if isNothingInferred(tree) =>
              tree -> "I feel a disturbance in the force, the type `Nothing` might have been inferred."

            // TODO: remove, it's too noisy
            //        case tree@Select(value, name) if unsafeOnEmptyIterable.contains(name.toString) && value.tpe <:< typeOf[Iterable[Any]] =>
            //          tree -> (
            //            s"Are you sure the `${value.tpe.typeSymbol.name}` will never be empty?\n" +
            //              s"Because calling `$name` might throw an exception in this case."
            //            )

            case tree@Select(ident, name) if name.toString == "get" && ident.tpe <:< typeOf[Option[Any]] =>
              tree -> s"""`$ident.get` can result in a `NoSuchElementException`, I recommend to write `$ident.getOrElse(sys.error("..."))`"""

            case tree@Select(value@Apply(Select(ident, n1), _), n2)
              if n1.toString == "get" && n2.toString == "getOrElse" && value.tpe <:< typeOf[Option[Any]] =>
              tree -> s"""`$ident.get(...).getOrElse(...)` can be simplified to `$ident.getOrElse(...)`."""

            case tree@Select(value@Apply(Select(ident, n1), _), n2)
              if n1.toString == "map" && n2.toString == "getOrElse" && value.tpe <:< typeOf[Option[Any]] =>
              tree -> s"""`$ident.get(...).getOrElse(...)` can be simplified to `$ident.getOrElse(...)`."""

            case tree@Select(ident, name) if name.toString == "find" && isMap(ident.tpe) =>
              tree -> s"`find` on a `Map` is O(n), you should use `$ident.get` instead."

            case tree@Select(ident, name) if name.toString == "find" && isSet(ident.tpe) =>
              tree -> s"`find` on a `Set` is O(n), you should use `$ident.get` instead."

            case tree@CaseClassArrayMembersExtractor((ident, members)) =>
              val names = members.map(_.name)
              tree -> (s"""`$ident`.{`${names.mkString(",")}`}: case class with `Array`s in c'tor: """ +
                s"""structural equality / hashing is not implemented. Use either `mutable.WrappedArray` or `IndexedSeq`.""")

            case tree@If(cond, Literal(Constant(true)), Literal(Constant(false))) =>
              tree -> s"`${briefTree(tree)}` can be simplified to `${briefTree(cond)}`."

            case tree@If(cond, Literal(Constant(false)), Literal(Constant(true))) =>
              tree -> s"`${briefTree(tree)}` can be simplified to `!${briefTree(cond)}`."

            case tree@Select(Apply(TypeApply(Select(ident, name1), typeArg :: Nil), arg :: Nil), name2)
              if name1.toString == "map" && name2.toString == "getOrElse" && typeArg.tpe =:= typeOf[Boolean] =>
              tree -> s"Simplifiable operation on collection: `$ident.exists(${briefTree(arg)}})`"

            //        case tree@Select(value, name) if name.toString == "foreach" =>
            //          println(tree.tpe)
            //          println(tree.tpe.prefix)
            //          println(tree.tpe.prefixChain)
            //          println(tree.tpe.typeSymbol)
            //          println(tree.tpe <:< typeOf[Range])
            //          println(tree.tpe <:< typeOf[scala.collection.immutable.Range])
            //          tree -> s"foreach"

            //        case tree@Select(v@Apply(value@Select(ident, n1), a1), n2) if n1.toString == "get" && n2.toString == "getOrElse" =>
            //          println(ident.tpe <:< typeOf[scala.collection.Map[Any, Any]])
            //          println(ident.tpe <:< typeOf[scala.Predef.Map[Any, Any]])
            //          println(show(ident.tpe))
            //          import BooleanFlag._
            //          println(showRaw(ident.tpe, printTypes = true)))
          }
    }

    override val examine: Seq[(String, Column => Position)] => Seq[(Position, Message)] = xs => {
      xs.collect {
        case (code, pos) if code.trim.endsWith(";") =>
          pos(code.length) -> "That `;` at the end of the line is unnecessary."
      }
    }
  }

}
