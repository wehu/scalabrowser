
import scala.xml.Unparsed
import javafx.application.Application
import scala.io.Source.fromURL
import scala.util.parsing.json._
import java.net.{URLConnection, URL}
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.io.Writer
import java.io.Reader
import java.io.BufferedReader
import java.io.InputStreamReader

import scala.xml.Node

import scalabrowser._

class WeiBo extends FXApp {
  title = "WeiBo"
  val clientID = ""
  val clientSecret = ""
  val redirectURI = "https://api.weibo.com/oauth2/default.html"
  val authURI = "https://api.weibo.com/2/oauth2"
  var code = ""
  var accessToken = ""
  abstract class OAuth2Phase
  case class AuthPhase() extends OAuth2Phase
  case class AuthPhaseDone() extends OAuth2Phase
  var authPhase: OAuth2Phase = null
  
  var encoding = "UTF-8"
  val app = new App {
    //val dq = DispatchQueue {}
    override def default = {
      authPhase = AuthPhase()
      load(authURI+"/authorize?"+
          "client_id="+clientID+
          "&response_type=code&display=default"+
          "&redirect_uri="+redirectURI)
    }
    override def onLoaded = {
      authPhase match {
        case _ : AuthPhase =>
          evalJS("app.setCode(document.URL.toString())")
          if(code != ""){         
            val resp = post(authURI+"/access_token?"+
              "client_id="+clientID+
              "&client_secret="+clientSecret+
              "&grant_type=authorization_code"+
              "&redirect_uri="+redirectURI+
              "&code="+code)
            resp match {
              case d: Map[_, _] => 
                val Some(v) = d.asInstanceOf[Map[String, String]].get("access_token")
                accessToken = v
              case _ =>
            }
            authPhase = AuthPhaseDone()
            loadContent(friendsTimeLinePage)
          }
        case _ =>
      }
    }
    def setCode(url: String) {
     // dq.dispatch[Unit] {
        val R = (redirectURI+"""\?code=(\S+)""").r
        url match {
          case R(at) =>
            code = at
          case _ => 
        }
    //  }
    }
      
    def friendsTimeLinePage = {
      val data = invokeWeiBoAPI("statuses/friends_timeline")
      toXML(data).toString()
    }
    
    def toXML(js: Any): Node = {
      js match {
        case d: Map[_, _] =>
          val children =  d.foldLeft(Seq[Node]()) {(seq, p)=>
            {seq :+ <li>{toXML(p._1 + " : ")}{toXML(p._2)}</li>}}
          <ul>{children}</ul>
        case d: List[_] =>
          val children =  d.foldLeft(Seq[Node]()) {(seq, c)=>
            {seq :+ <li>{toXML(c)}</li>}}
          <ul>{children}</ul>
        case d: String => <span>{d}</span>
        case d: Number => <span>{d}</span>
        case _ => <span></span>
      }
    }
    
    def JSONParse(str: String) : Any = {
      JSON.parseFull(str) match {
        case Some(data) => data
        case None => "Error"
      }
    }
    
    def get(url: String) : Any = {
      try {
        val resp = fromURL(url, encoding).mkString
        JSONParse(resp)
      } catch {
        case e => throw e
      }
    }
    

    def post(url: String): Any = {
      val u = new URL(url)
      val conn = u.openConnection
      conn.setDoOutput(true)
      conn.setConnectTimeout(5000)
      conn.connect
      val wr = new OutputStreamWriter(conn.getOutputStream())
      wr.flush
      wr.close
      val is = conn.getInputStream
      val writer: Writer = new StringWriter() 
      var buffer = Array[Char](1024)
      val reader: Reader = new BufferedReader(
         new InputStreamReader(is, encoding))
      var n = 0
      n = reader.read(buffer)
      while (n != -1) {
        writer.write(buffer, 0, n)
        n = reader.read(buffer)
      }
      is.close()
      val resp = writer.toString()
      JSONParse(resp)
    }
      
    def invokeWeiBoAPI(m: String, opts: Map[String, String] = Map()) : Any = {
      val params = opts.foldLeft("") {(i, p)=>{i + "&" + p._1 + "=" + p._2}}
      val url = "https://api.weibo.com/2/"+m+".json?access_token="+accessToken+params
      try {
        get(url)
      } catch {
        case e => throw e
      }
    }
    override def onClose = {
      //dq.stop
    }
  }
}

object main{
  def main(args: Array[String]) {
    Application.launch(classOf[WeiBo], args: _*)
  }
}
