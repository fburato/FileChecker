/**
 * File: Main.scala
 * Package: main
 * Autore: Francesco Burato
 * Creazione: 02/giu/2013
 */
package main

import java.io.{ File, PrintWriter }
import checksum.{ FileCheckerFactory, FileChecksummer }
import checkmanager.{ CheckManager, ThreadBasedManager, ActorBasedManager }
import workers.{ ProcessFiles, Completion, Terminate }
import scala.actors.Actor
import scala.actors.Actor.receive
/**
 * Programma principale
 * @author Francesco Burato
 *
 */
object Checksummer {

  def Problem(s: String) {
    println("Errore : " + s)
    System.exit(-1)
  }
  class Printer(path: String) extends Actor {
    var terminator: Actor = null
    def act() {
      // ottengo il totale dei file
      val total = receive {
        case ProcessFiles(tot) => tot
      }
      if (total == 0) {
        println("'" + path + "' non e' un path valido o leggibile")
        terminator ! Terminate()
        return
      }
      // total > 0
      println("Inizio elaborazione")
      var completed = 0
      var lastpercent = 0.0
      while (completed < total) {
        receive {
          case Completion(i) =>
            completed += 1
            val percent = (0.0 + completed) / total * 100
            if (percent >= lastpercent + 1) {
              lastpercent = percent
              println(percent + "% completato")
            }
        }
      }
      // concludo l'elaborazione
      terminator ! Terminate()
    }
  }

  class Executor(path: String, checker: CheckManager, output: File) extends Actor {
    def act() {
      val start = System.currentTimeMillis()
      val list = checker.execute(path)
      val stop = System.currentTimeMillis()
      receive {
        case Terminate() =>
      }
      // stampo il tempo trascorso
      if (list.length > 0) {
        println("Elaborazione completata in " + (stop - start) + " ms")
        println("Scrittura nel file output")
        val out = new PrintWriter(output)
        for ((file, res) <- list) {
          out.write(res + " <-- " + file + "\n")
        }
        out.close()
      }
    }
  }
  def main(args: Array[String]) {
    // controllo i permessi di scrittura sulla directory corrente
    val output = new File("./checksum.txt")
    if(!output.exists())
      if(!output.createNewFile())
        Problem("Impossibile scrivere il file output checksum.txt")
    else if (!output.canWrite())
      Problem("Impossibile scrivere il file output checksum.txt")
    // controllo gli argomenti da linea di comando
    /*
     * 1 : path da esaminare
     * [2 : tipo elaborazione "thread" o "actor" default=thread]
     * [3 : tipo di checksum default="update"]
     */
    if (args.length == 0)
      Problem("Sintassi: scala Checksummer Path [actor/thread] [tipo checksum]")
    val path = args(0)
    val typeProc = if (args.length >= 2)
      if (args(1) == "actor" || args(1) == "thread")
        args(1)
      else
        Problem("Metodo di esecuzione non riconosciuto: '" + args(1) + "'")
    else "actor"
    val checksum = if (args.length >= 3)
      args(2)
    else
      "update"

    // controllo dell'algoritmo di checksum
    val f = FileCheckerFactory.getInstance(checksum) match {
      case None => null
      case Some(c) => c
    }
    if (f == null)
      Problem("Algoritmo di checksum non riconosciuto: '" + checksum + "'")
    println("Paramentri di esecuzione:\n- Path: '" + path + "'\n- Metodo: '" + typeProc +
      "'\n- Checksum: '" + checksum + "'")
    // parametri controllati
    // istanzio gli attori di output
    val printer = new Printer(path)
    val checker: CheckManager = typeProc match {
      case "actor" => new ActorBasedManager(f, printer)
      case "thread" => new ThreadBasedManager(f, printer)
    }
    val executor = new Executor(path, checker, output)
    printer.terminator = executor
    printer.start
    executor.start
  }

}