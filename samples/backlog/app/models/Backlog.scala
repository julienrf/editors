package models

import play.api.libs.json._


sealed trait TaskState
case object Todo extends TaskState
case object Doing extends TaskState
case object Done extends TaskState

object TaskState {

  implicit val format: Format[TaskState] = Format(
    Reads(_.validate[String].map { case "Todo" => Todo case "Doing" => Doing case "Done" => Done }),
    Writes[TaskState] { case Todo => JsString("Todo") case Doing => JsString("Doing") case Done => JsString("Done") }
  )

}

case class Task(name: String, state: TaskState)

object Task {

  implicit val format = Json.format[Task]

}