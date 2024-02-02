//> using scala "3.3.1"
//> using dep "io.github.marcinzh::daae-core:0.2.0"
//> using dep "io.github.marcinzh::yamlist:0.2.0"
package demos
import turbolift.{!!, Handler}
import turbolift.Extensions._
import turbolift.effects.{Choice, Reader, WriterK, Console, IO}
import yamlist._
import daae.Debug


object TreeWalk:
  def main(args: Array[String]): Unit =
    case object Search extends Choice
    case object Log extends WriterK[Vector, String]
    case object Path extends Reader[List[String]]
    type Search = Search.type
    type Log = Log.type
    type Path = Path.type

    def visit(tree: Tree): Unit !! (Search & Log & Path & Debug) =
      for
        label <- Debug.pause("label")(tree.label)
        _ <- Log.tell(label)
        _ <- Path.localModify(label :: _):
          for
            _ <- Debug.traceEff("path")(Path.asks(_.reverse.mkString("/")))
            branch <- Search.choose(tree.branches)
            _ <- visit(branch)
          yield ()
      yield ()

    def select: Handler.FromId.Free[Vector, Search] !! (Console & IO) =
      for
        _ <- Console.println("Select search order: (d)epth-first, or (b)readth-first")
        input <- Console.readln
        h <- input match
          case ""|"d" => Search.handlers.all.pure_!!
          case "b" => Search.handlers.allBreadthFirst.pure_!!
          case _ => Console.println("Bye") &&! IO.cancel
      yield h

    select.flatMap: h =>
      visit(Tree.build)
      .handleWith(h)
    .handleWith(Path.handler(Nil))
    .handleWith(Log.handler.justState)
    .flatTap(xs => Console.println(s"Visited trees: ${xs.mkString(" ")}"))
    .handleWith(Debug.handler())
    .handleWith(Console.handler)
    .unsafeRun


  //=======================================================================

  case class Tree(label: String, branches: Vector[Tree]) derives Yamlement

  object Tree:
    object Syntax:
      extension (thiz: String) def tree(xs: Yamlist[Tree]): Tree = Tree(thiz, xs.unwrap)
      given Yamlement.Into[String, Tree] = Yamlement.Into(Tree(_, Vector()))
      given Yamlement[String] = Yamlement.derived

    def build: Tree =
      import Syntax.{given, _}

      "root".tree:
        - "A"
        - "B".tree:
          - "1"
          - "2"
        - "C"
        - "D"
        - "E".tree:
          - "3".tree:
            - "a"
            - "b"
        - "F".tree:
          - "4".tree:
            - "c"
            - "d"
          - "5".tree:
            - "e"
            - "f"
