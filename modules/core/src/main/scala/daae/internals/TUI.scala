package daae.internals
import turbolift.!!
import turbolift.Extensions._
import turbolift.effects.Console
import sourcecode.{FileName => File, Line}


object TUI:
  def trace(config: Config, label: String, value: => Any) =
    !!.when(config.trace):
      Console.println(s"${" Trace ".colored(C.tracefg, C.tracebg)} $label = ${value.toString}")

  def show(prompt: String, config: Config, file: File, line: Line, canReplace: Boolean, canGoBack: Boolean): Unit !! Console =
    val lineFmt = "%-5d ".format(line.value)
    val fileFmt = file.value.takeRight(L.w_file).padTo(L.w_file, ' ')
    Vector(
      (
        draw("╔", '═', "", L.top).frame +
        " Turbolift Debugger ".colored(C.titlefg, C.titlebg) +
        draw("", '═', "╗", L.top).frame
      ),
      "║ ".frame + "File: ".label + fileFmt.vary + " ║".frame,
      (
        "║ ".frame + 
        "Line: ".label + lineFmt.vary + sep +
        key("T", "race is ") + onOff(config.trace) + sep +
        key("P", "ause is ") + onOff(config.pause) +
        empty(L.pad_line) +
        " ║".frame
      ),
      (
        "║ ".frame + 
        key("Enter", " Resume") + sep +
        key("Go ", "b", "ack") + sep +
        key("R", "eplace") + sep +
        key("Q", "uit") +
        empty(L.pad_cmd) +
        " ║".frame
      ),
      draw("╚", '═', "╝", L.bot).frame,
    )
    .foreach_!!(Console.println)
    .&&!(Console.print(s"$prompt>"))

  def hide: Unit !! Console =
    val u = cursorUp(L.h)
    val l = (" " * L.w) + "\n"
    Console.print(u + (l * L.h) + u)

  private def key(a: String, b: String, c: String ): String = a.label + b.highl + c.label
  private def key(a: String, b: String): String = key("", a, b)
  private def sep = " | ".colored(C.deco)
  private def draw(a: String, b: Char, c: String, n: Int): String = a + (b.toString * n) + c
  private def onOff(x: Boolean) = if x then "ON".vary + " ".label else "OFF".vary
  private def empty(n: Int) = (" " * n).label
  private def cursorUp(n: Int) = s"\u001b[${n}A"

  private object L:
    val h = 6
    val w = 60
    val top = 12
    val bot = 44
    val w_file = 36
    val pad_line = 0
    val pad_cmd = 3

  extension (thiz: String)
    private def colored(fg: Int, bg: Int = C.backg) = RGB.fg(fg) + RGB.bg(bg) + thiz + Console.RESET
    private def frame = thiz.colored(C.frame)
    private def label = thiz.colored(C.label)
    private def vary = thiz.colored(C.vary)
    private def highl = Console.BOLD + thiz.colored(C.highl)

  private object RGB:
    def fg(n: Int) = "\u001b[38;2;" + rgb(n)
    def bg(n: Int) = "\u001b[48;2;" + rgb(n)
    private def rgb(n: Int) = s"${(n >> 16) & 255};${(n >> 8) & 255};${n & 255}m"

  private object C:
    val frame   = 0xdddddd
    val label   = 0xdddddd
    val backg   = 0x0044cc
    val deco    = 0x0022aa
    val vary    = 0xffdd00
    val highl   = 0x00ffff
    val tracefg = 0xffffff
    val tracebg = 0x884488
    val titlefg = 0xffffff
    val titlebg = backg
