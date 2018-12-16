package recommender.preprocess

import com.spotify.scio._
import source.beam.FileIO

class DataPreProcessHandler(@transient val sc: ScioContext,
                            val input: String,
                            val output: String) extends FileIO {
  def createFilePipelines(basePath: String): Unit = {
    val test = readCsv(sc, basePath + "test.csv")

//    val train = readCsv(sc, basePath + "train.csv")
//    val transactions = readCsv(sc, basePath + "historical_transactions.csv")
//    val merchants = readCsv(sc, basePath + "merchants.csv")
//    val merchants_transaction = readCsv(sc, basePath + "new_merchant_transactions.csv")

//    sc.close() // Show results for testing purposes
  }
}

object DataPreProcessHandle {
  def main(cmdlineArgs: Array[String]): Unit = {
    val (sc, args) = ContextAndArgs(cmdlineArgs)

    val input = args.getOrElse("input", "gs://some-bucket/data.csv")
    val output = args.getOrElse("output", "./output.txt")

    val dataProcessor = new DataPreProcessHandler(sc, input, output)
    dataProcessor.createFilePipelines("./data/raw/")
  }
}
