/**
 * File: Messages.scala
 * Package: workers
 * Autore: Francesco Burato
 * Creazione: 01/giu/2013
 */
package workers

import java.io.File

/**
 * Clase contenente i messaggi per la comunicazione 
 * @author Francesco Burato
 *
 */
case class CheckSum(f: File)
case class Response(f: File, s : String)
case class Terminate()
case class Completion(num : Int)
case class ProcessFiles(tot : Int)