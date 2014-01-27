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

  implicit def gen[A]: Keys[A] = macro ???

  def fields[A] = macro ???

  // Expansion of the `Keys.fields[User]` macro call
  def `fields[User]`(name: String = "name", age: String = "age") = Keys[User](name, age)
  implicit def defaultUserKeys = `fields[User]`()

}