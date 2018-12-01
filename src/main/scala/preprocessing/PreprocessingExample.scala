package preprocessing

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object PreprocessingExample {

  def main(args: Array[String]): Unit = {
    val input = "@khezamie He's escalating this. #Russia been making moves last couple days that suggest something bigger is on the herizon."
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
  }
}
