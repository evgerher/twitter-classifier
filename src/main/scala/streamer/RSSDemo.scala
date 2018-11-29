package streamer

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object RSSDemo {
  val msgs = new mutable.TreeSet[String]

  def main(args: Array[String]) {
    val durationSeconds = 15
    val conf = new SparkConf()
      .setAppName("RSS Spark Application")
      .setIfMissing("spark.master", "local[*]")
//      .set("spark.driver.bindAddress", "127.0.0.1")

    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(durationSeconds))
    sc.setLogLevel("ERROR")

    val urlCSV = args(0)
    val urls = urlCSV.split(",")
    val stream = new RSSInputDStream(urls, Map[String, String](
      "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    ), ssc, StorageLevel.MEMORY_ONLY, pollingPeriodInSeconds = durationSeconds)
    stream.foreachRDD(rdd=>{
      val spark = SparkSession.builder().appName(sc.appName).getOrCreate()
      import spark.sqlContext.implicits._
      rdd.toDS().select("title").collect().foreach(process_row)
    })

    // run forever
    ssc.start()
    ssc.awaitTermination()
  }

  def process_row(item: Row): Unit = {
//    println(item)
    val a: String = item.getString(0).trim
    println(s"The row is ${a}")
    if (!msgs.contains(a)) {
      println(s"New message came :: ${a}")
      msgs.add(a)
    }
  }
}
