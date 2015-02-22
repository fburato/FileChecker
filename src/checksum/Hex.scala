/**
 * File: Hex.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

/**
 * Classe di supporto per effettuare la conversione da array di byte a stringa esadecimale
 * @author Francesco Burato
 *
 */
object Hex {

  def valueOf(buf: Array[Byte]): Option[String] =
    if (buf != null)
      Some(buf.map("%02X" format _).mkString)
    else
      None
}