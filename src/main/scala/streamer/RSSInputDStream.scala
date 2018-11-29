package streamer

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver

class RSSInputDStream(feedURLs: Seq[String],
                      requestHeaders: Map[String, String],
                      ssc: StreamingContext,
                      storageLevel: StorageLevel,
                      connectTimeout: Int = 1000,
                      readTimeout: Int = 1000,
                      pollingPeriodInSeconds: Int = 60)
  extends ReceiverInputDStream[RSSEntry](ssc) with Logger{

  override def getReceiver(): Receiver[RSSEntry] = {
    println("Creating RSS receiver")
    logDebug("Creating RSS receiver")
    new RSSReceiver(
      feedURLs = feedURLs,
      requestHeaders = requestHeaders,
      storageLevel = storageLevel,
      connectTimeout = connectTimeout,
      readTimeout = readTimeout,
      pollingPeriodInSeconds = pollingPeriodInSeconds
    )
  }

}