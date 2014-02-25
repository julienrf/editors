package editors

import play.api.mvc._
import play.api.data.mapping.{Success, Validation}

trait Editors extends Uis {

  /**
   * Typeclass defining an editor for a given data type `A`
   * @tparam A
   */
  trait Editor[A] {

    def ui: UiType

    def formUi(action: Call) =
      <form action={ action.url } method={ action.method }>
        { ui }
      </form>

    def bind(data: Map[String, Seq[String]]): Validation[String, A]

  //   def unbind: Option[A]

    def submission[B](f: A => SimpleResult)(implicit DataReader: DataReader[B]): Action[B] = Action(DataReader.bodyParser) { implicit request =>
      bind(DataReader.read(request.body)) match {
        case Success(a) => f(a)
        case _ => Results.UnprocessableEntity
      }
    }

  }

  object Editor {

    import language.experimental.macros

    @inline def apply[A : Editor]: Editor[A] = implicitly[Editor[A]]

    implicit def gen[A](implicit Ui: Ui[A], Mapping: Mapping[A]): Editor[A] = macro ???

    // Expansion of the `gen[User]` macro call
    implicit def `gen[User]`(implicit Ui: Ui[User], Mapping: Mapping[User]): Editor[User] = new Editor[User] {
      def ui = Ui.ui
      def bind(data: Map[String, Seq[String]]) = Mapping.bind(data)
    }

  }

}