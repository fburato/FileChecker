/**
 * File: ActorTest.scala
 * Package: test
 * Autore: Francesco Burato
 * Creazione: 01/giu/2013
 */
package test
import checkmanager._
import checksum._
import scala.actors.Actor._
import workers._

object ActorTest {
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
    val manager = new ActorBasedManager(factory.getInstance("md5").get,progress)
    val l = manager.execute("/Users/francescoburato/scala")
    for( i <- l)
      println(i)
  }
}