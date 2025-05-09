package daae.internals
import turbolift.!!
import turbolift.Extensions._
import turbolift.bindless._
import turbolift.effects.{Console, IO}
import sourcecode.{FileName => File, Line}
import daae.{DebugEffect, DebugSignature, MaybeFromString}

//==========================================================================================
// Derived from Unison code: handler for `Debug` ability from `Stepwise` project.
// https://share.unison-lang.org/@pchiusano/stepwise/code/releases/1.0.0/latest/types/Debug
//==========================================================================================

case class Config(trace: Boolean = true, pause: Boolean = true)

object DebugHandler:
  def apply[Fx <: DebugEffect](fx: Fx, initial: Config): fx.ThisHandler[Identity, Identity, Console & IO] =
    new fx.impl.Stateful[Identity, Identity, Console & IO] with fx.impl.Sequential with DebugSignature:
      override type Local = (Config, Option[Breakpoint[?]])

      override def onInitial = (initial, None).pure_!!

      override def onReturn(a: Unknown, s: Local) = a.pure_!!

      override def traceEff[A, U <: ThisEffect](label: String)(body: A !! U) =
        `do`:
          val (config, _) = Local.get.!
          val value = body.!
          if config.trace then TUI.trace(label, value).!
          value

      override def pauseEff[A, U <: ThisEffect](label: String)(body: A !! U)(using line: Line, file: File, mfs: MaybeFromString[A]) =
        `do`:
          val (config, lastBreakpoint) = Local.get.!
          val value = body.!
          if !config.pause then
            value
          else
            Control.captureGet[A, Ambient & Console & IO]: (k, s) =>
              val (config, lastBreakpoint) = s
              new Breakpoint(
                label = label,
                value = value,
                file = file,
                line = line,
                resumeRec = thisBreakpoint => (a, c) => k(a, (c, Some(thisBreakpoint))),
                previous = lastBreakpoint,
              )
              .go(config)
            .!

      class Breakpoint[A](
        label: String,
        value: A,
        file: File,
        line: Line,
        resumeRec: Breakpoint[A] => (A, Config) => Unknown !! (Ambient & Console & IO),
        previous: Option[Breakpoint[?]],
      )(using mfs: MaybeFromString[A]):
        def go(config: Config): Unknown !! (Ambient & Console & IO) =
          def loop(config: Config, value: A, prompt: String = "Awaiting command"): Unknown !! (Ambient & Console & IO) =
            interact(config, prompt).map(_.toLowerCase).flatMap:
              case "" => resume(value, config)
              case "b" => previous match
                case None => loop(config, value, "Can't go back before the first breakpoint")
                case Some(breakpoint) => breakpoint.go(config)
              case "r" =>
                if mfs.isDefined then
                  `do`:
                    mfs.fromString(interact(config, "Enter new value to resume with").!) match
                      case None => loop(config, value, "Invalid value").!
                      case Some(value2) =>
                        TUI.pause(label, value2).!
                        loop(config, value2, "Value replaced").!
                else
                  loop(config, value, "Parser for values of this type is not available")
              case "t" => loop(config.copy(trace = !config.trace), value)
              case "p" => loop(config.copy(pause = !config.pause), value)
              case "q" => IO.cancel
              case x => loop(config, value, s"Invalid command `$x`, try again")
          `do`:
            TUI.pause(label, value).!
            loop(config, value).!

        private def resume = resumeRec(this)

        private def interact(config: Config, prompt: String): String !! Console =
          `do`:
            TUI.show(prompt, config, file, line, canReplace = mfs.isDefined, canGoBack = previous.isDefined).!
            val input = Console.readln.!
            TUI.hide.!
            input

    .toHandler
