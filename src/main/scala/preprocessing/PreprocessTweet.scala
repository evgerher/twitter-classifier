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

    val resultList = tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("\\W+")
      .filter(_.matches("^[a-zA-Z ]+$"))
      .filterNot(stopWordsSet)
    var sb: StringBuilder = new StringBuilder()
    for (i <- resultList.indices) {
      sb.append(resultList(i))
      if (i != resultList.length - 1) {
        sb.append(" ")
      }
    }
    val result = sb.toString()
      .split("")
      .toList
    sb = new StringBuilder()
    var count = 0
    for (i <- 1 until result.length) {
      if (result(i) != result(i - 1)
        || (i != result.length - 1 && count < 2 && result(i) != result(i + 1))) {
        sb.append(result(i - 1))
        if (i == result.length - 1) {
          sb.append(result(i))
        }
        count = 0
      } else if (i == result.length - 1) {
        sb.append(result(i))
      } else {
        count += 1
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
