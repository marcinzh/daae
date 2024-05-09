package daae
import turbolift.{!!, Signature, Effect}
import turbolift.effects.{Console, IO}
import sourcecode.{FileName => File, Line}
import daae.internals.{DebugHandler, Config}


trait DebugSignature extends Signature:
  /** Creates breakpoint after execution of [[body]]. */
  def pauseEff[A, U <: ThisEffect](label: String)(body: A !! U)(using Line, File, MaybeFromString[A]): A !! U

  /** Like [[pauseEff]], except for it actually doesn't. */
  def traceEff[A, U <: ThisEffect](label: String)(body: A !! U): A !! U


trait DebugEffect extends Effect[DebugSignature] with DebugSignature:
  final override def pauseEff[A, U <: this.type](label: String)(body: A !! U)(using l: Line, f: File, mfs: MaybeFromString[A] = MaybeFromString.none): A !! U = perform(_.pauseEff(label)(body))
  final override def traceEff[A, U <: this.type](label: String)(body: A !! U): A !! U = perform(_.traceEff(label)(body))

  final def pause[A](label: String)(value: => A)(using l: Line, f: File, mfs: MaybeFromString[A] = MaybeFromString.none): A !! this.type = pauseEff(label)(!!.impure(value))
  final def trace[A](label: String)(value: => A): A !! this.type = traceEff(label)(!!.impure(value))

  /** Predefined handler for this effect. */
  final def handler(trace: Boolean = true, pause: Boolean = true): ThisHandler[Identity, Identity, Console & IO] =
    DebugHandler(this, Config(trace = trace, pause = pause))


/** Predefined instance of [[DebugEffect]]. */
case object Debug extends DebugEffect
type Debug = Debug.type
