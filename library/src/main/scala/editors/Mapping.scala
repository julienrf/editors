package editors

import scala.util.Try

/**
 * Bidirectional mapping from and to HTTP requests data
 * @tparam A
 */
abstract class Mapping[A] {
  /**
   * Bind an `A` value from the given `data`.
   * @param data Data to bind the value from. It has type `Seq[String]` because the default HTTP content type (form-urlencoded) allows to map several values to a given key.
   * @return Some value if the binding was successful, otherwise `None`
   */
  def bind(key: String, data: Map[String, Seq[String]]): Option[A]

  /**
   * @param value Value to unbind
   * @return A string representation of the value
   */
  def unbind(value: A): String
}

object Mapping {

  implicit val stringMapping: Mapping[String] = new Mapping[String] {
    def bind(key: String, data: Map[String, Seq[String]]) = data.get(key).flatMap(_.headOption)
    def unbind(value: String) = value
  }

  implicit val intMapping: Mapping[Int] = new Mapping[Int] {
    def bind(key: String, data: Map[String, Seq[String]]) = Try(data.get(key).flatMap(_.headOption.map(_.toInt))).toOption.flatten
    def unbind(value: Int) = value.toString
  }

  implicit val doubleMapping: Mapping[Double] = new Mapping[Double] {
    def bind(key: String, data: Map[String, Seq[String]]) = Try(data.get(key).flatMap(_.headOption.map(_.toDouble))).toOption.flatten
    def unbind(value: Double) = value.toString
  }

  def `fields[User]`(name: Mapping[String] = implicitly[Mapping[String]], age: Mapping[Int] = implicitly[Mapping[Int]])(implicit Keys: Keys[User]) = new Mapping[User] {
    def bind(key: String, data: Map[String, Seq[String]]) =
      for {
        nameValue <- name.bind(Keys.keys(0), data)
        ageValue <- age.bind(Keys.keys(1), data)
      } yield User(nameValue, ageValue)
    def unbind(value: User) = ???
  }
  implicit def defaultUserMapping(implicit Keys: Keys[User], stringMapping: Mapping[String], intMapping: Mapping[Int]) = `fields[User]`()

}

