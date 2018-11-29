import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object Preprocessing {

  def main(args: Array[String]): Unit = {
    val input = "晚安"
    val result = input.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("")
      .filter(_.matches("^[a-zA-Z ]+$")).toList
    val sb: StringBuilder = new StringBuilder()
    for (i <- 1 until result.length) {
      if (result(i) != result(i - 1)) {
        sb.append(result(i - 1))
        if (i == result.length - 1) {
          sb.append(result(i))
        }
      }
    }
    print(sb.toString())
    // everything that is in main is just for testing
//    val durationSeconds = 15
//    val conf = new SparkConf()
//      .setAppName("RSS Spark Application")
//      .setIfMissing("spark.master", "local[*]")
//    //      .set("spark.driver.bindAddress", "127.0.0.1")
//
//    val sc = new SparkContext(conf)
//    val ssc = new StreamingContext(sc, Seconds(durationSeconds))
//    sc.setLogLevel("ERROR")
//    val input = "Some input, @someone hello! juuust is"
//    val preprocess: PreprocessTweet = new PreprocessTweet(ssc)
//    print(preprocess.preprocessText(input))

  }
}
