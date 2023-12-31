package daae
import turbolift.{!!, Signature, Effect}
import turbolift.effects.{Console, IO}
import sourcecode.{FileName => File, Line}
import daae.internals.{DebugHandler, Config}


trait DebugSignature extends Signature:
  /** Creates breakpoint after execution of [[body]]. */
  def pause[A, U <: ThisEffect](label: String)(body: A !! U)(using Line, File, MaybeFromString[A]): A !@! U

  /** Like [[pause]], except for it actually doesn't. */
  def trace[A, U <: ThisEffect](label: String)(body: A !! U): A !@! U


trait DebugEffect extends Effect[DebugSignature] with DebugSignature:
  final override def pause[A, U <: this.type](label: String)(body: A !! U)(using l: Line, f: File, mfs: MaybeFromString[A] = MaybeFromString.none): A !! U = perform(_.pause(label)(body))
  final override def trace[A, U <: this.type](label: String)(body: A !! U): A !! U = perform(_.trace(label)(body))

  final def pausePure[A](label: String)(value: => A)(using l: Line, f: File, mfs: MaybeFromString[A] = MaybeFromString.none): A !@! this.type = pause(label)(!!.impure(value))
  final def tracePure[A](label: String)(value: => A): A !@! this.type = trace(label)(!!.impure(value))

  /** Predefined handler for this effect. */
  final def handler(trace: Boolean = true, pause: Boolean = true): ThisHandler[[X] =>> X, [X] =>> X, Console & IO] =
    DebugHandler(this, Config(trace = trace, pause = pause))


/** Predefined instance of [[DebugEffect]]. */
case object Debug extends DebugEffect
type Debug = Debug.type
