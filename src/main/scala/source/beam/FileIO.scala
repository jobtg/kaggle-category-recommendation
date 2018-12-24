package source.beam

import java.nio.channels.Channels

import scala.io.Source.fromInputStream
import source.string.StringTransformer
import com.spotify.scio.ScioContext
import com.spotify.scio.coders.Coder
import com.spotify.scio.values.SCollection
import org.apache.beam.sdk.io.FileSystems
import org.apache.beam.sdk.options.PipelineOptionsFactory

class FileIO extends StringTransformer with Serializable {
  implicit def coderObject: Coder[Object] = Coder.kryo[Object]

  private def keyValueSplit(columnNames: Seq[String], keyIndex: Int, sep: String)(line: String): Seq[Object] = {
    val lineSplit = line.split(sep)

    Seq(lineSplit(keyIndex), columnNames.zip(lineSplit.map(_.matchDataType)).toMap)
  }

  private def readCsvColumnHeaders(path: String,
                                   sep: String = ",",
                                   header: Boolean = true): Array[String] = {
    // Register GS filesystem.
    val options = PipelineOptionsFactory.create()
    FileSystems.setDefaultPipelineOptions(options)

    val src = Channels
      .newInputStream(
        FileSystems.open(
          FileSystems
            .matchSingleFileSpec(path)
            .resourceId()))

    val headerLine = fromInputStream(src).getLines.next.split(sep)

    src.close()

    if (header) headerLine else headerLine.zipWithIndex.map { case (_, i) => s"c_$i" }
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
      .map(keyValueSplit(columnNames, keyIndex, sep))
  }

}
