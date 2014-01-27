package editors

import play.api.mvc._
import scala.xml.NodeSeq

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

    def bind(data: Map[String, Seq[String]]): Option[A]

  //   def unbind: Option[A]

    def submission[B](f: A => SimpleResult)(implicit DataReader: DataReader[B]): Action[B] = Action(DataReader.bodyParser) { implicit request =>
      bind(DataReader.read(request.body)) match {
        case Some(a) => f(a)
        case None => Results.UnprocessableEntity
      }
    }

  }

  object Editor {

    import language.experimental.macros

    implicit def gen[A](implicit UiModule: Uis): Editor[A] = macro ???

    // Expansion of the `gen[User]` macro call
    def `gen[User]`()(implicit Ui: Ui[User], Mapping: Mapping[User]) = new Editor[User] {
      def ui = Ui.ui
      def bind(data: Map[String, Seq[String]]) = Mapping.bind("", data)
    }

  }

}