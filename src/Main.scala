import javafx.application.Application
import scala.xml.Unparsed

import scalabrowser._

class Test extends FXApp {
  title = "test"
  width = 300
  height = 80
  val app = new App {
    override def default = {
      loadContent(<span>
<script src="http://code.jquery.com/jquery-latest.js"></script>
<span>
name: <input type="text" id="name"></input>
<input type="submit" value="Submit" id="query"></input>
<hr></hr>
info: <span id="output"></span>
<script type="text/javascript">{Unparsed("""
  $(document).ready(function (){
    $("#query").on("click", function(){
      var v = $("#name").val();
      $("#name").val("");
      app.info(v);
      app.test(v);
    });
  });
    """)}
</script>
</span>
</span>)
    }
    def test(in: String){
      evalJS("""$("#output").html("<span>""" + in + "</span>\")")
    }
  }
}

object main{
  def main(args: Array[String]) {
    Application.launch(classOf[Test], args: _*)
  }
}