package editors

import scala.language.higherKinds

import scala.xml.{Node, NodeSeq}

/**
 * Base interface for presentational aspects of editors
 */
trait Uis {

  type Presentation[A] <: PresentationLike[A]

  trait PresentationLike[A]

  /**
   * Presentational data needed for this look (e.g. label, input name, placeholder, etc.)
   */
  type FieldPresentation

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
    def ui(data: FieldPresentation): UiType
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

  // TODO Factor out duplicated code with BootstrapLook
  trait Presentation[A] extends PresentationLike[A] {
    private[editors] def presentations: Seq[FieldPresentation]
  }

  object Presentation {

    import language.experimental.macros

    implicit def gen[A]: Presentation[A] = ???

    def fields[A] = macro ???

    def apply[A](ps: FieldPresentation*): Presentation[A] = new Presentation[A] {
      val presentations = ps
    }

  }

  type FieldPresentation = String // just the input name

  case class Ui[A](ui: NodeSeq) extends UiLike[A]

  trait FieldUi[A] extends FieldUiLike[A]

  object FieldUi {

    def apply[A](f: String => NodeSeq): FieldUi[A] = new FieldUi[A] {
      def ui(data: FieldPresentation) = f(data)
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

  trait Presentation[A] extends PresentationLike[A] {
    private[editors] def presentations: Seq[FieldPresentation]
  }

  object Presentation {

    import language.experimental.macros

    implicit def gen[A]: Presentation[A] = ???

    def fields[A] = macro ???

    def apply[A](ps: FieldPresentation*): Presentation[A] = new Presentation[A] {
      val presentations = ps
    }

    def `FieldPresentation[User.name]` = FieldPresentation("name", Some("Name:"), None, Some("Enter name"), Seq.empty)
    def `FieldPresentation[User.age]` = FieldPresentation("age", Some("Age:"), None, Some("Enter age"), Seq.empty)
    def `fields[User]`(name: FieldPresentation = `FieldPresentation[User.name]`, age: FieldPresentation = `FieldPresentation[User.age]`): Presentation[User] = Presentation[User](name, age)
    implicit def `gen[User]`: Presentation[User] = `fields[User]`()

  }

  case class FieldPresentation(key: String,
                    label: Option[String],
                    hint: Option[String],
                    placeholder: Option[String],
                    validationRules: Seq[???])

  case class Ui[A](ui: NodeSeq) extends UiLike[A]

  object Ui {

    import scala.language.experimental.macros

    implicit def gen[A : Mapping : Key : Presentation]: Ui[A] = ???

    def fields[A : Mapping : Key : Presentation] = macro ???

    // FIXME Do I need to depend on Mapping and Key?
    def `fields[User]`(name: FieldUi[String] = implicitly[FieldUi[String]], age: FieldUi[Int] = implicitly[FieldUi[Int]])(implicit Mapping: Mapping[User], Key: Key[User], Presentation: Presentation[User]): Ui[User] =
      Ui[User](look.append(name.ui(Presentation.presentations(0)), age.ui(Presentation.presentations(1))))

    implicit def `gen[User]`(implicit Mapping: Mapping[User], Key: Key[User], Presentation: Presentation[User]): Ui[User] = `fields[User]`()

  }

  trait FieldUi[A] extends FieldUiLike[A]

  object FieldUi {
    implicit val uiInt: FieldUi[Int] = new FieldUi[Int] {
      def ui(data: FieldPresentation) = formGroup(data, input("number", data))
    }

    implicit val uiDouble: FieldUi[Double] = new FieldUi[Double] {
      def ui(data: FieldPresentation) = formGroup(data, input("number", data))
    }

    implicit val uiBoolean: FieldUi[Boolean] = new FieldUi[Boolean] {
      def ui(data: FieldPresentation) = formGroup(data, input("checkbox", data))
    }

    implicit val uiString: FieldUi[String] = new FieldUi[String] {
      def ui(data: FieldPresentation) = formGroup(data, input("text", data))
    }

    def input(`type`: String, data: FieldPresentation) =
      <input name={ data.key } type={ `type` } placeholder={ data.placeholder getOrElse "" } />

    def formGroup(data: FieldPresentation, input: Node) =
      <div class="form-group">
        <label>
          { data.label getOrElse "" }
          { input }
        </label>
      </div>
  }

}