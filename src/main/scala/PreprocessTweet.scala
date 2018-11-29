import org.apache.spark.streaming.{Seconds, StreamingContext}

class PreprocessTweet {

  val stopWordsSet: Set[String] = null

  def this(ssc: StreamingContext) {
    this()
    val stopFile = ssc.sparkContext.textFile("file:///C:/Users/Aline/IdeaProjects/twitter-classifier/src/main/resources/stop-words.txt")
    val stopWordsSet = stopFile.collect().toSet
  }

  def preprocessText(tweetText: String): String = {
    //    some manipulations with Dataset
    val processedTweetList = tweetText.toLowerCase()
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
    for (i <- 1 until processedTweetList.length) {
      if (processedTweetList(i) != processedTweetList(i - 1)) {
        sb.append(processedTweetList(i - 1))
        if (i == processedTweetList.length - 1) {
          sb.append(processedTweetList(i))
        }
      }
    }
    sb.toString()
  }
}
