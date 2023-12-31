package daae


trait MaybeFromString[A]:
  def fromString(s: String): Option[A]
  def isDefined: Boolean


object MaybeFromString:
  def apply[A](using mfs: MaybeFromString[A]): MaybeFromString[A] = mfs

  def none[A]: MaybeFromString[A] = new:
    override def fromString(s: String): Option[A] = None
    override def isDefined: Boolean = false

  trait Defined[A] extends MaybeFromString[A]:
    final override def isDefined: Boolean = true

  given MaybeFromString[String] = new Defined:
    override def fromString(s: String): Option[String] = Some(s)

  given MaybeFromString[Int] = new Defined:
    override def fromString(s: String): Option[Int] = s.toIntOption

  given MaybeFromString[Boolean] = new Defined:
    override def fromString(s: String): Option[Boolean] = s.toBooleanOption
