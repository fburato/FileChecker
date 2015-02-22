package test
import checkmanager._
import checksum._
import workers._
import scala.actors.Actor._


object ThreadTest {
  def main(argv: Array[String]){
    val factory = FileCheckerFactory
    val progress = actor {
      var total = 0
      receive {
        case ProcessFiles(tot) => total = tot
      }
      var completed = 0
      print("0/" + total)
      while(completed < total) {
        receive {
          case Completion(i) => 
            completed += 1
            print(" -> " + completed + "/" + total )
        }
      }
      println
    }
    val manager = new ThreadBasedManager(factory.getInstance("md5").get,progress)
    val l = manager.execute("/Users/francescoburato/scala/learning")
    for( i <- l)
      println(i)
  }
}