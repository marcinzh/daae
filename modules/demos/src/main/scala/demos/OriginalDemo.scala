//> using scala "3.3.1"
//> using dep "io.github.marcinzh::daae-core:0.2.0"
package demos
import turbolift.effects.Console
import daae.Debug

//==========================================================================================
// Scala reimplementation of Unison demo for `Debug` ability from `Stepwise` project.
// https://share.unison-lang.org/@pchiusano/stepwise/code/releases/1.0.0/latest/types/Debug
//==========================================================================================

object OriginalDemo:
  def main(args: Array[String]): Unit =
    println:
      (for
        x <- Debug.pause("x"):
          1 + 2  // As shown in the video. It's different in the repository linked above: `1 + 1`
        y <- Debug.pauseEff("y"):
          for
            z <- Debug.pause("what's this?"):
              99 + 1
          yield x + x + z
      yield x + y)
      .handleWith(Debug.handler(true, true))
      .handleWith(Console.handler)
      .unsafeRun
