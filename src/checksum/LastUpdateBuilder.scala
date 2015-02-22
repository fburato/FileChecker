/**
 * File: LastUpdateBuilder.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

/**
 * @author Francesco Burato
 *
 */
object LastUpdateBuilder extends FileCheckBuilder {
  private val l = new LastUpdateChecksummer
  def getInstance(): FileChecksummer = l

}