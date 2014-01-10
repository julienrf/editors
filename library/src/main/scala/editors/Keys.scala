package editors

trait Keys[A] {
  private[editors] def keys: Seq[String]
}

object Keys {

  import language.experimental.macros

  def apply[A : Keys]: Keys[A] = implicitly[Keys[A]]

  def apply[A](ks: String*): Keys[A] = new Keys[A] {
    val keys = ks
  }

  implicit def default[A]: Keys[A] = macro ???

  implicit def fields[A] = macro ???

}