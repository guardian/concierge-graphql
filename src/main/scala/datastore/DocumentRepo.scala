package datastore

import schema.Content

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Content.Document]

}
