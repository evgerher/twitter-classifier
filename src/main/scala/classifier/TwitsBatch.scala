package classifier

import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}

class TwitsBatch(spark: SparkSession, twits: Array[String]) {
  def getDataSet(): Unit = {
    val rows = twits.map{x => Row(x:_*)}
    val rdd = spark.sparkContext.makeRDD[Row](rows)

    val fieldList = List(
      StructField("twits", StringType, true)
    )

    val schema = StructType.apply(fieldList)

    val df = spark.createDataFrame(rdd, schema)
  }
}
