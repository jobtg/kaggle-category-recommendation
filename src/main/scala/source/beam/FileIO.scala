package source.beam

import source.string.StringTransformer.matcher._
import com.spotify.scio.ScioContext
import com.spotify.scio.coders.Coder
import com.spotify.scio.values.SCollection

import scala.io.Source

class FileIO {
  implicit def coderObject: Coder[Object] = Coder.kryo[Object]

  private def readCsvColumnHeaders(path: String,
                                   sep: String = ",",
                                   header: Boolean = true): Array[String] = {
    val src = Source.fromFile(path)
    val line = src.getLines.next().split(sep)

    src.close()

    if (header) line else line.zipWithIndex.map { case (_, i) => s"c_$i" }
  }

  def readCsv(sc: ScioContext,
              path: String,
              keyColumn: String = "card_id",
              sep: String = ",",
              header: Boolean = true): SCollection[Seq[Object]] = {
    val columnNames = readCsvColumnHeaders(path, sep, header)
    val keyIndex = columnNames.indexOf(keyColumn)

    sc
      .textFile(path)
      .map(
        x => Seq(
          x.split(",")(keyIndex),
          columnNames.zip(x.split(",").map(_.matchDataType)).toMap))
  }

}
