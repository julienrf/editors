package editors

trait Key[A] {
  private[editors] def keys: Seq[String]
}

object Key {

  import language.experimental.macros

  def apply[A : Key]: Key[A] = implicitly[Key[A]]

  def apply[A](ks: String*): Key[A] = new Key[A] {
    val keys = ks
  }

  implicit def gen[A]: Key[A] = macro ???

  def fields[A] = macro ???

  // Expansion of the `Key.fields[User]` macro call
  def `fields[User]`(name: String = "name", age: String = "age") = Key[User](name, age)
  implicit def `gen[User]`: Key[User] = `fields[User]`()

}