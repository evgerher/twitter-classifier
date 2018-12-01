package classifier

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

class TwitsBatch(spark: SparkSession, twits: List[String]) {
  def getDataSet(): DataFrame = {
    val rows = twits.map{x => Row(x:_*)}
    val rdd = spark.sparkContext.makeRDD[Row](rows)

    val fieldList = List(
      StructField("classifier/twits", StringType, true)
    )

    val schema = StructType.apply(fieldList)

    return spark.createDataFrame(rdd, schema)
  }
}
