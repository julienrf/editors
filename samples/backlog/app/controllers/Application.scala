package controllers

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import play.modules.reactivemongo.json.collection.JSONCollection
import models.Task
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action

object Application extends ReactiveMongoAutoSourceController[Task] {

  val coll = db.collection[JSONCollection]("backlog")

  val index = Action {
    Ok
  }

}
