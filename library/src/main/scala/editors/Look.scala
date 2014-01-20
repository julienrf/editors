package editors

import scala.language.higherKinds

import scala.xml.NodeSeq

/**
 * Base interface for presentational aspects of editors
 */
trait Look {

  /**
   * Presentational data needed for this look (e.g. label, input name, placeholder, etc.)
   */
  type FieldData

  type UiType = NodeSeq

  /**
   * It is left as an abstract type member so you can define your own typeclass and its companion object containing common implicit instances
   * @tparam A
   */
  type Ui[A] <: UiLike[A]

  /**
   * Typeclass defining the HTML form for a given data type
   * @tparam A
   */
  trait UiLike[A] {
    def ui(data: FieldData): UiType
  }

  // TODO Make it extensible (and abstract over NodeSeq)
  object look {
    def append(fields: UiType*) = fields.fold(NodeSeq.Empty)(_ ++ _)
  }

}

/**
 * No label, hint or error message. Just input fields.
 */
trait SimpleLook extends Look {

  type FieldData = String // just the input name

  trait Ui[A] extends UiLike[A]

  object Ui {
    implicit val uiInt: Ui[Int] = new Ui[Int] {
      def ui(key: String) = input(key, "number")
    }

    implicit val uiDouble: Ui[Double] = new Ui[Double] {
      def ui(key: String) = input(key, "number")
    }

    implicit val uiBoolean: Ui[Boolean] = new Ui[Boolean] {
      def ui(key: String) = input(key, "checkbox")
    }

    implicit val uiString: Ui[String] = new Ui[String] {
      def ui(key: String) = input(key)
    }

    import scala.language.experimental.macros

    case class Deps[A](Keys: Keys[A])

    case class FieldDeps(key: String)

    def apply[A](uip: String => NodeSeq): Ui[A] = new Ui[A] {
      def ui(key: String) = uip(key)
    }

    def fields[A : Keys] = macro ???

    // Look specific helper to build an input for a given field
    def input(key: String, `type`: String = "text", attrs: Seq[(String, String)] = Seq.empty[(String, String)]) = <input type={ `type` } name={ key } />

    // Expansion of the `Ui.fields[User]` macro call
    def `fields[User]`(name: Ui[String] = implicitly[Ui[String]], age: Ui[Int] = implicitly[Ui[Int]])(implicit Keys: Keys[User]) = new Ui[User] {
      def ui(key: String) = look.append(name.ui(Keys.keys(0)), age.ui(Keys.keys(1))) // Why is the `key` parameter ignored?
    }
    implicit def defaultUserUi(implicit Keys: Keys[User], stringUi: Ui[String], intUi: Ui[Int]) = `fields[User]`()

  }

}

/**
 * Twitter Bootstrap look
 */
trait TwitterBootstrapLook extends Look {

  case class FieldData(key: String,
                    label: Option[String],
                    hint: Option[String],
                    placeholder: Option[String])

  trait Ui[A] extends UiLike[A]

  object Ui {
    implicit val uiInt: Ui[Int] = new Ui[Int] {
      def ui(data: FieldData) = <input type="number" name={ data.key } />
    }

    implicit val uiDouble: Ui[Double] = new Ui[Double] {
      def ui(data: FieldData) = <input type="number" name={ data.key } />
    }

    implicit val uiBoolean: Ui[Boolean] = new Ui[Boolean] {
      def ui(data: FieldData) = <input type="checkbox" name={ data.key } />
    }

    implicit val uiString: Ui[String] = new Ui[String] {
      def ui(data: FieldData) = <input type="text" name={ data.key } />
    }
  }

}