/**
 * File: FileChecksummer.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum
import java.io.File
import checkmanager.Configurator
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.DigestInputStream
/**
 * Classe che realizza il checksum md5 di un file
 * @author Francesco Burato
 *
 */
class Md5Checksummer extends FileChecksummer {
  private val md = MessageDigest.getInstance("MD5")
  def sum(f : File) : String = {
    val dis = new DigestInputStream(new FileInputStream(f),md)
    val buffer = new Array[Byte](Configurator.bufferSize)
    while(dis.read(buffer)!= -1) {} //leggo tutto il file
    val digest = md.digest();
    Hex.valueOf(digest) match {
      case Some(s) => s
      case None    => ""
    }
  }
}