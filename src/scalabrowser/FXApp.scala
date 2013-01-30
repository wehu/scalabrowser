package scalabrowser

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.scene.layout.Region
import javafx.event.EventHandler
import javafx.scene.layout.Priority
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import netscape.javascript.JSObject
import javafx.stage.WindowEvent;

abstract class FXApp extends Application {
  
  trait App {
    private var _webEngine: WebEngine = null
    def default = {
      loadContent("Hello World")
    }
    def onInit = {}
    def onLoaded = {}
    def onClose = {}
    final def webEngine: WebEngine = {
      if(_webEngine == null) {throw new Exception("Unknown webEngine")}
      _webEngine
    }
    final def webEngine_=(we: WebEngine){
      _webEngine = we
    }
    final def loadContent(content: scala.xml.Node){
      webEngine.loadContent(content.toString())
    }
    final def loadContent(content: String){
      webEngine.loadContent(content)
    }
    final def load(url: String){
      webEngine.load(url)
    }
    final def info(v: String){
      println(v)
    }
    final def evalJS(s: String): JSObject = {
      webEngine.executeScript(s).asInstanceOf[JSObject]
    }
  }
  
  class Browser(app: App) extends Region {
    final val view = new WebView()
    final val webEngine = view.getEngine()
    app.webEngine= webEngine
    
    webEngine.getLoadWorker().stateProperty().addListener(
      new ChangeListener[State]() {
        override def changed(ov: ObservableValue[_ <: State],
                    oldState: State, newState: State) {   
          if (newState == State.SUCCEEDED) {
            val win = webEngine.executeScript("window")
            win.asInstanceOf[JSObject].setMember("app", app)
            app.onLoaded
          }
        }
      }
    )
    
    app.default
    getChildren().add(view)
    
    override protected def layoutChildren() : Unit = {
      val w = getWidth()
      val h = getHeight()
      layoutInArea(view,0,0,w,h,0,HPos.CENTER,VPos.CENTER);
    }
  }
  
  val app:App
  var title = "Untitled"
  var width = 750
  var height = 500
  var bgColor = Color.web("#666970")
  
  override def start(stage: Stage) {
    stage.setTitle(title)
    val browser = new Browser(app)
    val scene = new Scene(browser, width, height, bgColor)
    stage.setScene(scene)
    app.onInit;
    stage.show()
    stage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      def handle(we: WindowEvent) {
          app.onClose;
      }
    })
  }
  
}
