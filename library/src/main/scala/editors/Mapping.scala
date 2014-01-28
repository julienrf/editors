package editors

import scala.util.Try

/**
 * Bidirectional mapping from and to HTTP requests data
 * @tparam A
 */
trait Mapping[A] {
  /**
   * Bind an `A` value from the given `data`.
   * @param data Data to bind the value from. It has type `Seq[String]` because the default HTTP content type (form-urlencoded) allows to map several values to a given key.
   * @return Some value if the binding was successful, otherwise `None`
   */
  def bind(data: Map[String, Seq[String]]): Try[A]

//  /**
//   * @param value Value to unbind
//   * @return A string representation of the value
//   */
//  def unbind(value: A): String
}

object Mapping {

  import scala.language.experimental.macros

  implicit def gen[A]: Mapping[A] = macro ???

  def fields[A] = macro ???

  // TODO Use a more sophisticated data type for Key so that keys access is type safe
  def `fields[User]`(name: FieldMapping[String] = implicitly[FieldMapping[String]], age: FieldMapping[Int] = implicitly[FieldMapping[Int]])(implicit Keys: Key[User]) = new Mapping[User] {
    def bind(data: Map[String, Seq[String]]) =
      for {
        nameValue <- name.bind(data.get(Keys.keys(0)) getOrElse Nil)
        ageValue <- age.bind(data.get(Keys.keys(1)) getOrElse Nil)
      } yield User(nameValue, ageValue)
//    def unbind(value: User) = ???
  }
  implicit def `gen[User]`(implicit Keys: Key[User], stringMapping: FieldMapping[String], intMapping: FieldMapping[Int]): Mapping[User] = `fields[User]`()

}

trait FieldMapping[A] {

  def bind(data: Seq[String]): Try[A]

  def unbind(value: A): String

}

object FieldMapping {

  implicit val stringMapping: FieldMapping[String] = new FieldMapping[String] {
    def bind(data: Seq[String]) = Try(data.head)
    def unbind(value: String) = value
  }

  implicit val intMapping: FieldMapping[Int] = new FieldMapping[Int] {
    def bind(data: Seq[String]) = Try(data.head.toInt)
    def unbind(value: Int) = value.toString
  }

  implicit val doubleMapping: FieldMapping[Double] = new FieldMapping[Double] {
    def bind(data: Seq[String]) = Try(data.head.toDouble)
    def unbind(value: Double) = value.toString
  }

}
