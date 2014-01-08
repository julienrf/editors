package editors

import scala.language.higherKinds

import scala.xml.NodeSeq

/**
 * Base interface for presentational aspects of editors
 */
trait Look {

  /**
   * Presentational data needed for this look
   */
  type FieldData

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
    def ui(data: FieldData): NodeSeq
  }

  // TODO Make it extensible (and abstract over NodeSeq)
  object look {
    def append(fields: NodeSeq*) = fields.fold(NodeSeq.Empty)(_ ++ _)
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
      def ui(key: String) = <input type="number" name={ key } />
    }

    implicit val uiDouble: Ui[Double] = new Ui[Double] {
      def ui(key: String) = <input type="number" name={ key } />
    }

    implicit val uiBoolean: Ui[Boolean] = new Ui[Boolean] {
      def ui(key: String) = <input type="checkbox" name={ key } />
    }

    implicit val uiString: Ui[String] = new Ui[String] {
      def ui(key: String) = <input type="text" name={ key } />
    }
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