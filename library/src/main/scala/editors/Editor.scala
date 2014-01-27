package editors

import play.api.mvc._
import scala.xml.NodeSeq

trait Editors extends Look {

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

    implicit def gen[A]: Editor[A] = macro ???

//    type EditorMaker[A] = String => Editor[A]
//
//    def apply[A, B](field1: (String, EditorMaker[A]), field2: (String, EditorMaker[B])): Editor[(A, B)] = new Editor[(A, B)] {
//
//      val aEditor = field1._2(field1._1)
//      val bEditor = field2._2(field2._1)
//
//      def ui = aEditor.ui ++ bEditor.ui
//
//      def bind(data: Map[String, Seq[String]]): Option[(A, B)] =
//        for {
//          a <- aEditor.bind(data)
//          b <- bEditor.bind(data)
//        } yield (a, b)
//
//    }
//
//    def apply[A](implicit Mapping: Mapping[A], UiData: UiData[A]): EditorMaker[A] = key => new Editor[A] {
//
//      def ui = UiMaker(FieldData(key)).ui
//
//      def bind(data: Map[String, Seq[String]]) =
//        for {
//          d <- data.get(key)
//          s <- Mapping.bind(d)
//        } yield s
//
//    }
//
//    def text(implicit UiMaker: UiMaker[String], Mapping: Mapping[String]): EditorMaker[String] = apply[String]
//
//    def number(implicit UiMaker: UiMaker[Int], Mapping: Mapping[Int]): EditorMaker[Int] = apply[Int]

  }

}