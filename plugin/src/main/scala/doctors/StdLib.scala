package drscala
package doctors

import scala.tools.nsc.Global
import scala._
import java.net.{URL, URLClassLoader}

trait StdLibComponent {
  self: HealthCake =>

  import self.global._

  val config = new Context()
  println(config)
  
  implicit private def isWarning(config: ConfigType): Boolean = config.isWarning

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

    object IsDefinedGetExtractor {
      def unapply(tree: Tree): Option[Set[Tree]] = tree match {
        case tree@If(cond, thenp, elsep) =>
          val isDefinedIdentifiers = cond.collect {
            case tree@Select(ident, name) if name.toString == "isDefined" && ident.tpe <:< typeOf[Option[Any]] => ident
          }

          val getIdentifiers = cond.collect {
            case tree@Select(ident, name) if name.toString == "get" && ident.tpe <:< typeOf[Option[Any]] => ident
          }

          val isDefinedGets = isDefinedIdentifiers.toSet.union(getIdentifiers.toSet)
          if (isDefinedGets.isEmpty) None else Some(isDefinedGets)
        case _ => None
      }
    }

    private def checkNoReturnType(tree: Tree): Seq[((Line, Column), Message)] = {

      def overridden(name: Name, baseClasses: List[Symbol], vparamss: List[List[ValDef]]): Boolean = {
        baseClasses.exists {
          symbol: Symbol =>
            symbol.tpe.members.exists {
              s: Symbol =>
                s.name == name && s.tpe.paramss == vparamss
            }
        }
      }

      def check(baseClasses: List[Symbol], impl: Template): Seq[(Position, String)] = {
        impl.body.collect {
          case tree@DefDef(Modifiers(0L, tpnme.EMPTY, _), name, tparams, vparamss, tpt: TypeTree, rhs)
            if !tree.symbol.isConstructor && tree.symbol.isPublic &&
              tpt.original == null &&
              !(tpt.tpe =:= typeOf[Unit]) &&
                overridden(name, baseClasses, vparamss) =>

            (tree.pos.line, tree.pos.column) -> s"The `public` method `$name` should have explicit return type, `$tpt` was inferred. Please specify return type."
        }
      }

      val c = tree.collect {
        case tree@ClassDef(mods, name, tparams, impl) =>
          check(tree.symbol.baseClasses, impl).toSeq

        case tree@ModuleDef(mods, name, impl) =>
          check(tree.symbol.baseClasses, impl).toSeq

        case _ =>
          Seq.empty[(Position, String)]
      }
      c.flatten
    }

    override val diagnostic: PartialFunction[PhaseId, CompilationUnit => Seq[(Position, Message)]] = {
      case "parser" => _.body.collect {
        case tree@Ident(name) if name.toString == "$qmark$qmark$qmark" && config.missingImplementation =>
          tree -> "Oops, an implementation is missing here."

        case tree@Apply(Ident(name), _) if name.toString == "println" && config.println =>
          tree -> "There is rarely a good reason to use `println`, is it the case here?"

        case tree@Select(_, name) if name.toString == "asInstanceOf" && config.azInstanceOf =>
          tree -> "An `asInstanceOf` could result in a `ClassCastException` at runtime, it's better to use a pattern match."
      }
      case "typer" =>
        u =>
          val classChecks: Seq[(Position, Message)] = if (config.checkNoReturnType) {
            checkNoReturnType(u.body)
          } else {
            Seq()
          }
          val bodyChecks: Seq[(Position, Message)] = u.body.collect {
            case tree if config.nothingInferred.isWarning && isNothingInferred(tree) =>
              tree -> "I feel a disturbance in the force, the type `Nothing` might have been inferred."

            case tree@Select(value, name) if config.unsafeOnEmptyIterable &&
            unsafeOnEmptyIterable.contains(name.toString) && value.tpe <:< typeOf[Iterable[Any]] =>
              tree -> (
                s"Are you sure the `${value.tpe.typeSymbol.name}` will never be empty?\n" +
                s"Because calling `$name` might throw an exception in this case."
              )

            case tree@Select(value@Apply(Select(ident, n1), _), n2)
              if config.getGetOrElse &&
                n1.toString == "get" && n2.toString == "getOrElse" && value.tpe <:< typeOf[Option[Any]] =>
              tree -> s"""`$ident.get(...).getOrElse(...)` can be simplified to `$ident.getOrElse(...)`."""

            case tree@Select(Apply(TypeApply(Select(ident, name1), typeArg :: Nil), arg :: Nil), name2)
              if config.mapGetOrElse &&
                name1.toString == "map" && name2.toString == "getOrElse" && typeArg.tpe =:= typeOf[Boolean] =>
              tree -> s"Simplifiable operation on collection, rewrite to: `$ident.exists(${briefTree(arg)}})`"

            case tree@Select(ident, name) if config.findOnSet && name.toString == "find" && isSet(ident.tpe) =>
              tree -> s"`find` on a `Set` is O(n), you should use `$ident.get` instead."

            case tree@Select(ident, name) if config.findOnMap && name.toString == "find" && isMap(ident.tpe) =>
              tree -> s"`find` on a `Map` is O(n), you should use `$ident.get` instead."

            case tree@CaseClassArrayMembersExtractor((ident, members)) if config.caseClassWithArray =>
              val names = members.map(_.name)
              tree -> (s"""`$ident`.{`${names.mkString(",")}`}: case class with `Array`s in c'tor: """ +
                s"""structural equality / hashing is not implemented. Use either `mutable.WrappedArray` or `IndexedSeq`.""")

            case tree@If(cond, Literal(Constant(true)), Literal(Constant(false))) if config.simplifyIf =>
              tree -> s"`${briefTree(tree)}` can be simplified to `${briefTree(cond)}`."

            case tree@If(cond, Literal(Constant(false)), Literal(Constant(true))) if config.simplifyIf =>
              tree -> s"`${briefTree(tree)}` can be simplified to `!${briefTree(cond)}`."

            case tree@IsDefinedGetExtractor(idents) if config.isDefinedGet =>
              tree -> s"""You can use a patten match `${idents.mkString(",")} match (...)` instead of `isDefined... get`."""

          }
          classChecks ++ bodyChecks
    }

    override val examine: Seq[(String, Column => Position)] => Seq[(Position, Message)] = xs => {
      xs.collect {
        case (code, pos) if code.trim.endsWith(";") =>
          pos(code.length) -> "That `;` at the end of the line is unnecessary."
      }
    }
  }

}
