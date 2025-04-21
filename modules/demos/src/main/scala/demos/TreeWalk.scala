//> using scala "3.3.5"
//> using dep "io.github.marcinzh::daae-core:0.8.0"
//> using dep "io.github.marcinzh::yamlist:0.2.0"
package demos
import turbolift.{!!, Handler}
import turbolift.Extensions._
import turbolift.effects.{Choice, Reader, WriterK, Console, IO}
import turbolift.bindless._
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
      `do`:
        val label = Debug.pause("label")(tree.label).!
        Log.tell(label).!
        Path.localModify(label :: _):
          `do`:
            val _ = Debug.traceEff("path")(Path.asks(_.reverse.mkString("/"))).!
            val branch = Search.choose(tree.branches).!
            visit(branch).!
        .!

    def select: Handler[Identity, Vector, Search, Any] !! (Console & IO) =
      `do`:
        Console.println("Select search order: (d)epth-first, or (b)readth-first").!
        Console.readln.! match
          case ""|"d" => Search.handlers.all
          case "b" => Search.handlers.allBreadthFirst
          case _ => Console.println("Bye").!; IO.cancel.!

    `do`:
      val h = select.!
      visit(Tree.build).handleWith(h).!
    .handleWith(Path.handler(Nil))
    .handleWith(Log.handler.justState)
    .tapEff(xs => Console.println(s"Visited trees: ${xs.mkString(" ")}"))
    .handleWith(Debug.handler())
    .handleWith(Console.handler)
    .runIO


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
