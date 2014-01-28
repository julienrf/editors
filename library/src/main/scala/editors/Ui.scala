package editors

import scala.language.higherKinds

import scala.xml.NodeSeq

/**
 * Base interface for presentational aspects of editors
 */
trait Uis {

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
    def ui: UiType
  }

  type FieldUi[A] <: FieldUiLike[A]

  trait FieldUiLike[A] {
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
trait SimpleLook extends Uis {

  type FieldData = String // just the input name

  case class Ui[A](ui: NodeSeq) extends UiLike[A]

  trait FieldUi[A] extends FieldUiLike[A]

  object FieldUi {

    def apply[A](f: String => NodeSeq): FieldUi[A] = new FieldUi[A] {
      def ui(data: FieldData) = f(data)
    }

    implicit val uiInt: FieldUi[Int] = new FieldUi[Int] {
      def ui(key: String) = input(key, "number")
    }

    implicit val uiDouble: FieldUi[Double] = new FieldUi[Double] {
      def ui(key: String) = input(key, "number")
    }

    implicit val uiBoolean: FieldUi[Boolean] = new FieldUi[Boolean] {
      def ui(key: String) = input(key, "checkbox")
    }

    implicit val uiString: FieldUi[String] = new FieldUi[String] {
      def ui(key: String) = input(key)
    }

    // Look specific helper to build an input for a given field
    def input(key: String, `type`: String = "text", attrs: Map[String, String] = Map.empty) = <input type={ `type` } name={ key } />

  }

  object Ui {

    import scala.language.experimental.macros

    implicit def gen[A](implicit Key: Key[User]): Ui[A] = ???

    def fields[A : Key] = macro ???

    // Expansion of the `Ui.fields[User]` macro call
    def `fields[User]`(name: FieldUi[String] = implicitly[FieldUi[String]], age: FieldUi[Int] = implicitly[FieldUi[Int]])(implicit Key: Key[User]): Ui[User] =
      Ui[User](look.append(name.ui(Key.keys(0)), age.ui(Key.keys(1))))
    // Expansion of `Ui.gen[User]`
    implicit def `gen[User]`(implicit Keys: Key[User], stringFieldUi: FieldUi[String], intFieldUi: FieldUi[Int]): Ui[User] = `fields[User]`()

  }

}

/**
 * Twitter Bootstrap look
 */
trait TwitterBootstrapLook extends Uis {

  case class FieldData(key: String,
                    label: Option[String],
                    hint: Option[String],
                    placeholder: Option[String],
                    validationRules: ???)

  case class Ui[A](ui: NodeSeq) extends UiLike[A]

  object Ui {

    import scala.language.experimental.macros

    implicit def gen[A](implicit Mapping: Mapping[A], Key: Key[User]): Ui[A] = ???

    def fields[A : Mapping : Key] = macro ???

    def `fields[User]`(name: FieldUi[String] = implicitly[FieldUi[String]], age: FieldUi[Int] = implicitly[FieldUi[Int]])(implicit Mapping: Mapping[User], Key: Key[User]): Ui[User] =
      Ui[User](look.append(name.ui(???), age.ui(???)))

    implicit def `gen[User]`(implicit Mapping: Mapping[User], Key: Key[User]): Ui[User] = `fields[User]`()

  }

  trait FieldUi[A] extends FieldUiLike[A]

  object FieldUi {
    implicit val uiInt: FieldUi[Int] = new FieldUi[Int] {
      def ui(data: FieldData) = <input type="number" name={ data.key } />
    }

    implicit val uiDouble: FieldUi[Double] = new FieldUi[Double] {
      def ui(data: FieldData) = <input type="number" name={ data.key } />
    }

    implicit val uiBoolean: FieldUi[Boolean] = new FieldUi[Boolean] {
      def ui(data: FieldData) = <input type="checkbox" name={ data.key } />
    }

    implicit val uiString: FieldUi[String] = new FieldUi[String] {
      def ui(data: FieldData) = <input type="text" name={ data.key } />
    }
  }

}