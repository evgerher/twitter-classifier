package streamer

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import classifier._

object RSSDemo {
  val set = new mutable.TreeSet[String]
  val batch = new ListBuffer[String]()
  val durationSeconds = 15
  val sparkSession: SparkSession = initSpark()
  val ssc: StreamingContext = initStreamingContext(sparkSession)

  def initSpark(): SparkSession = {
    val conf = new SparkConf()
      .setAppName("RSS Spark Application")
      .setIfMissing("spark.master", "local[*]")
    //      .set("spark.driver.bindAddress", "127.0.0.1")

    val sc = new SparkContext(conf)
    val sparkSession = SparkSession.builder.config(conf).getOrCreate()

    sc.setLogLevel("ERROR")
    (sparkSession)
  }

  def initStreamingContext(ss: SparkSession): StreamingContext = {
    (new StreamingContext(ss.sparkContext, Seconds(durationSeconds)))
  }

  def main(args: Array[String]) {
//    initSpark()

    val urlCSV = args(0)
    val urls = urlCSV.split(",")
    val stream = new RSSInputDStream(urls, Map[String, String](
      "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    ), ssc, StorageLevel.MEMORY_ONLY, pollingPeriodInSeconds = durationSeconds)
    stream.foreachRDD(rdd=>{
      val spark = SparkSession.builder().appName(sparkSession.sparkContext.appName).getOrCreate()
      import spark.sqlContext.implicits._
      //      rdd.toDS().select("title").collect().foreach(process_row)
        rdd
        .toDS()
        .select("title")
        .map(process_row)
        .filter(_.length > 0)
        .foreach(println(_))
    })

    // run forever
    ssc.start()
    ssc.awaitTermination()
  }

  def process_row(item: Row): String = {
//    println(item)
    val a: String = item.getString(0).trim
//    println(s"The row is ${a}")
    if (!set.contains(a)) {
//      println(s"New message came :: ${a}")
      set.add(a)
      return a
    } else {
      return ""
    }
  }

  def process_twits(list: List[String]): Unit = {
    val df = new TwitsBatch(sparkSession, list).getDataSet()
    df.collect().foreach(println)
  }
}
