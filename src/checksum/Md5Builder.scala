/**
 * File: Md5Builder.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

/**
 * Builder per effettuare la costruzione di nuovi Md5Checksummers
 * @author Francesco Burato
 *
 */
object Md5Builder extends FileCheckBuilder {
  def getInstance(): FileChecksummer = new Md5Checksummer
}