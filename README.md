# editors

UI framework for engineering data editors.

## Installation

```scala
libraryDependencies += "com.julienrf" %% "editors" % "0.1-SNAPSHOT"
```

## Usage

Create a case class for your data type:

```scala
case class Entry(name: String, duration: Int)
```

Generate an editor (here using a [Twitter bootstrap](http://getbootstrap.com/) theme):

```scala
object Entry extends Editors with TwitterBootstrapLook {
  val editor = Editor[Entry]()
}
```

Use it in your [Play!](http://www.playframework.com/) application:

```scala
object Entries extends Controller {

  val index = Action {
    Ok(views.entry.list(database.all()))
  }

  val form = Action {
    Ok(views.entry.form(Entry.editor.formUi))
  }

  val submit = Entry.editor.submission[AnyContent] { entry =>
    database.save(entry)
    Redirect(routes.Entries.index)
  }

}
```

Use it with [autosource](https://github.com/mandubian/play-autosource):

```scala
???
```

## Build from sources

Clone the source code and run sbt from the repository root directory:

```sh
$ git clone git@github.com:julienrf/editors.git
$ cd editors
$ sbt
```

Run the tests:

```
> test
```

Or run a sample:

```
> project sample-backlog
[sample-backlog] $ run
```
