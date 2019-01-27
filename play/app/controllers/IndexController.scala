package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import models.Todo
import daos.TodoDao
import javax.inject.{ Inject, Singleton }
import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, Controller }

case class TodoForm(action: String, content: String)

@Singleton
class IndexController @Inject() (val todoDao: TodoDao, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val todoForm = Form(
    mapping(
      "action" -> text,
      "content" -> text)(TodoForm.apply)(TodoForm.unapply))

  /**
   * 初期表示(GET)
   */
  def get = Action.async {
    todoDao.all().map(todos => Ok(views.html.index(todoForm, todos)))
  }

  /**
   * アクション(POST)
   */
  def post = Action.async { implicit request =>
    todoForm.bindFromRequest.fold(
      formWithErrors => {
        todoDao.all().map(todos => Ok(views.html.index(formWithErrors, todos)))
      },
      todoData => {
        val action = todoData.action.split(":")
        action(0) match {
          case "insert" => insert(todoData)
          case "update" => update(action(1), todoData)
          case "delete" => delete(action(1))
          case _ => println("No Action!!")
        }
        todoDao.all().map(todos => Ok(views.html.index(todoForm.fill(todoData), todos)))
      })
  }

  /** 登録 */
  def insert(todoForm: TodoForm) = todoDao.insert(Todo(0, todoForm.content))

  /** 更新 */
  def update(id: String, todoForm: TodoForm) = todoDao.update(Todo(id.toLong, todoForm.content))

  /** 削除 */
  def delete(id: String) = todoDao.delete(Todo(id.toLong, ""))

}
