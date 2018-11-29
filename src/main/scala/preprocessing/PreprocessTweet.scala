package preprocessing

import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable

class PreprocessTweet {
  var set = new mutable.TreeSet[String]
  var stopWordsSet: Set[String] = null

  def this(ssc: StreamingContext) {
    this()
    val stopFile = ssc
      .sparkContext
      .textFile("file:///C:/cygwin64/home/evger/twitter-classifier/src/main/resources/stop-words.txt")
    this.stopWordsSet = stopFile.collect().toSet
  }

  def preprocessText(item: Row): String = {
    val tweetText: String = item.getString(0).trim

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
        if (i == processedTweetList.length - 1 || i == 0) {
          sb.append(processedTweetList(i))
        }
      }
    }
    val filtered = sb.toString()

    if (filtered.length == 0)
      return filtered

    if (set.contains(filtered)) {
      return ""
    } else {
      set.add(filtered)
      println(s"ORIGINAL :: ${tweetText}")
      return filtered
    }
  }
}
