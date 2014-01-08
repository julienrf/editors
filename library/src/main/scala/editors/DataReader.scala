package editors

import play.api.mvc.{BodyParsers, AnyContentAsFormUrlEncoded, AnyContent, BodyParser}

/**
 * Read data of type `A` as a `Map[String, Seq[String] ]`
 * @tparam A
 */
trait DataReader[A] {
  def read(a: A): Map[String, Seq[String]]
  def bodyParser: BodyParser[A]
}

object DataReader {

  implicit val anyContentReader = new DataReader[AnyContent] {
    def read(a: AnyContent) = a match {
      case AnyContentAsFormUrlEncoded(data) => data
    }
    def bodyParser = BodyParsers.parse.anyContent
  }

}
