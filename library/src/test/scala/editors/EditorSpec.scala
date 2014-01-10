package editors

import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call
import editors.bakery.Application

object EditorSpec extends Specification with Editors with SimpleLook {

  "Editor" should {

    case class User(name: String, age: Int)

    // Expansion of the `Keys.fields[User]` macro call
    def `Keys.fields[User]`(name: String = "name", age: String = "age") = Keys[User](name, age)

    // Expansion of the `Ui.fields[User]` macro call
    def `Ui.fields[User]`(name: Ui[String] = implicitly[Ui[String]], age: Ui[Int] = implicitly[Ui[Int]])(implicit Keys: Keys[User]) = new Ui[User] {
      def ui(key: String) = look.append(name.ui(Keys.keys(0)), age.ui(Keys.keys(1))) // Why is the `key` parameter ignored?
    }

    def `Mapping.fields[User]`(name: Mapping[String] = implicitly[Mapping[String]], age: Mapping[Int] = implicitly[Mapping[Int]])(implicit Keys: Keys[User]) = new Mapping[User] {
      def bind(key: String, data: Map[String, Seq[String]]) =
        for {
          nameValue <- name.bind(Keys.keys(0), data)
          ageValue <- age.bind(Keys.keys(1), data)
        } yield User(nameValue, ageValue)
      def unbind(value: User) = ???
    }

    // Expansion of the `Editor[User]` macro call
    def `Editor[User]`()(implicit Ui: Ui[User], Mapping: Mapping[User]) = new Editor[User] {
      def ui = Ui.ui("")
      def bind(data: Map[String, Seq[String]]) = Mapping.bind("", data)
    }

    // Expansion of the implicit macro materializers
    implicit def defaultUserKeys = `Keys.fields[User]`()
    implicit def defaultUserMapping = `Mapping.fields[User]`()
    implicit def defaultUserUi = `Ui.fields[User]`()

    "generate forms for record types" in {

      "automatically" in {
        val editor = `Editor[User]`()
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "with custom presentation data" in {
        implicit val userKeys = `Keys.fields[User]`(name = "user_name", age = "user_age")
        val editor = `Editor[User]`()
        println(editor.ui)
        editor.ui.contains(<input type="text" name="user_name" />) must beTrue
        editor.ui.contains(<input type="number" name="user_age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "whole form" in {
        val editor = `Editor[User]`
        val form = editor.formUi(Call("POST", "/submit"))
        form.label must equalTo ("form")
        form.attribute("method").get.apply(0).text must equalTo ("POST")
        form.attribute("action").get.apply(0).text must equalTo ("/submit")
        form.descendant.contains(<input type="text" name="name" />) must beTrue
        form.descendant.contains(<input type="number" name="age" />) must beTrue
      }
    }

    "bind data from form submission" in {
      val editor = `Editor[User]`
      "successfully" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("28"))) must beSome (User("julien", 28))
      }
      "with failure" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("twenty-height"))) must beNone
      }
      "from within a Play action" in {
        val submissionAction = editor.submission[AnyContent](data => Results.Ok)
        def request(data: (String, String)*) = FakeRequest().withFormUrlEncodedBody(data: _*)

        "successfully" in {
          val result = submissionAction(request("name" -> "julien", "age" -> "28"))
          status(result) must equalTo (OK)
        }

        "with failure" in {
          val result = submissionAction(request("name" -> "julien", "age" -> "twenty-height"))
          status(result) must equalTo (422)
        }
      }
    }

    // editor.foo must equalTo (???)

    // editor.bar must equalTo (???)

    "support validation constraints" in pending ("(TODO)")

    "handle bidirectional relations" in pending("(TODO)")

    "support feedback logic definition" in pending("(TODO)")

    "do something in an app" in {
      status(Application.showForm(FakeRequest())) must equalTo (200)
    }

  }

}
