package preprocessing

import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object PreprocessDataset {
  def preprocess(session: SparkSession, src: String): Dataset[Row] = {
    val processor: PreprocessTweet = new PreprocessTweet(session)

    val data = session.read
      .format("csv")
      .option("header", "true")
      .load(src)


    data.show(20)

    val schema = StructType(Seq(
      StructField("ItemID", StringType),
      StructField("Sentiment", StringType),
      StructField("SentimentText", StringType)
    ))


    val encoder = RowEncoder(schema)

    val test = session.createDataset(data.rdd
        .map(row => {
          Row(row.getAs[String](0), row.getAs[String](1), processor.preprocessText(row.getAs[String](2)))
        })
    )(encoder)

    test.show(20)

    return test
  }
}
