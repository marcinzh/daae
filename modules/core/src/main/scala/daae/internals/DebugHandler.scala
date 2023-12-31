package daae.internals
import turbolift.{!!, Handler}
import turbolift.Extensions._
import turbolift.effects.{Console, IO}
import sourcecode.{FileName => File, Line}
import daae.{DebugEffect, DebugSignature, MaybeFromString}

//==========================================================================================
// Derived from Unison code: handler for `Debug` ability from `Stepwise` project.
// https://share.unison-lang.org/@pchiusano/stepwise/code/releases/1.0.0/latest/types/Debug
//==========================================================================================

case class Config(trace: Boolean = true, pause: Boolean = true)

object DebugHandler:
  def apply[Fx <: DebugEffect](fx: Fx, initial: Config): Handler[[X] =>> X, [X] =>> X, fx.type, Console & IO] =
    new fx.impl.Stateful[[X] =>> X, Console & IO] with fx.impl.Sequential with DebugSignature:
      override type Stan = (Config, Option[Breakpoint[?]])

      override def onInitial = (initial, None).pure_!!

      override def onReturn(a: Unknown, s: Stan) = a.pure_!!

      override def trace[A, U <: ThisEffect](label: String)(body: A !! U) =
        (k, s) =>
          k.escapeAndCapture(body, s).flatMap:
            case (value, k, (lastConfig, _)) =>
              TUI.trace(lastConfig, label, value) &&! k(value)

      override def pause[A, U <: ThisEffect](label: String)(body: A !! U)(using line: Line, file: File, mfs: MaybeFromString[A]) =
        (k, s) =>
          k.escapeAndCapture(body, s).flatMap:
            case (value, k, (lastConfig, lastBreakpoint)) =>
              new Breakpoint(
                label = label,
                value = value,
                file = file,
                line = line,
                resumeRec = thisBreakpoint => (a, c) => k(a, (c, Some(thisBreakpoint))),
                previous = lastBreakpoint,
              )
              .go(lastConfig)

      class Breakpoint[A](
        label: String,
        value: A,
        file: File,
        line: Line,
        resumeRec: Breakpoint[A] => (A, Config) => Unknown !! Ambient,
        previous: Option[Breakpoint[?]],
      )(using mfs: MaybeFromString[A]):
        def go(config: Config): Unknown !! Ambient =
          for
            _ <- TUI.trace(config, label, value)
            u <- 
              if !config.pause then
                resume(value, config)
              else
                def loop(config: Config, prompt: String = "Awaiting command"): Unknown !! Ambient =
                  interact(config, prompt).map(_.toLowerCase).flatMap:
                    case "" => resume(value, config)
                    case "b" => previous match
                      case None => loop(config, "Can't go back before the first breakpoint")
                      case Some(breakpoint) => breakpoint.go(config)
                    case "r" =>
                      if mfs.isDefined then
                        interact(config, "Enter new value to resume with").map(mfs.fromString).flatMap:
                          case None => loop(config, "Invalid value")
                          case Some(value) => resume(value, config)
                      else
                        loop(config, "Parser for values of this type is not available")
                    case "t" => loop(config.copy(trace = !config.trace))
                    case "p" => loop(config.copy(pause = !config.pause))
                    case "q" => IO.cancel
                    case x => loop(config, s"Invalid command `$x`, try again")
                loop(config)
          yield u

        private def resume = resumeRec(this)

        private def interact(config: Config, prompt: String): String !! Console =
          for
            _ <- TUI.show(prompt, config, file, line, canReplace = mfs.isDefined, canGoBack = previous.isDefined)
            input <- Console.readln
            _ <- TUI.hide
          yield input

    .toHandler
