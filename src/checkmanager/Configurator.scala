/**
 * File: Configurator.scala
 * Package: checkmanager
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checkmanager

/**
 * Configuratore generale dell'applicazione
 * @author Francesco Burato
 *
 */
object Configurator {
  private var myPoolSize = 4
  private var myBufferSize = 4096
  def workerPoolSize = myPoolSize
  def bufferSize = myBufferSize
  def workerPoolSize_=(i : Int) : Int = {
    if(i>0)
      myPoolSize = i
    myPoolSize
  }
  
  def bufferSize_=(i : Int) : Int = {
    if(i>0)
      myBufferSize = i
    myBufferSize
  }
}