import org.apache.spark.streaming.{Seconds, StreamingContext}

object Preprocessing {

  def main(args: Array[String]): Unit = {

    // everything that is in main is just for testing

    val input = "Some input, @someone hello! juuust"
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

  def preprocessText(ssc: StreamingContext, tweetText: String): Unit = {
    //    some manipulations with Dataset

    val stopFile = ssc.sparkContext.textFile("src/main/resources/stop-words.txt")
    val stopWordsSet = stopFile.collect().toSet
    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("")
      .filter(_.matches("^[a-zA-Z ]+$"))
      .filterNot(stopWordsSet).toList
    val sb: StringBuilder = new StringBuilder()
    for (i <- 1 until tweetText.length) {
      if (tweetText(i) != tweetText(i - 1)) {
        sb.append(tweetText(i - 1))
        if (i == tweetText.length - 1) {
          sb.append(tweetText(i))
        }
      }
    }

  }
}
