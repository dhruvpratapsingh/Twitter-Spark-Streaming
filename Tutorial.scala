import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import TutorialHelper._

object Tutorial {
  def main(args: Array[String]) {
    
    // Checkpoint directory
    val checkpointDir = TutorialHelper.getCheckpointDirectory()

    // Configure Twitter credentials
    val apiKey = "MJvkBpGhIPO04KjAsWCPhILhD"
    val apiSecret = "VebUSxAsuG9nY0ksRfPV73Ejwb9NUvVOe0ynd7aLZbMKzKgeC8"
    val accessToken = "3031039721-9Abm6Y7NFB65b4gq6FVIhhF1zfRfiwTnagH763I"
    val accessTokenSecret = "BMuEHm8H5bxwTKOLj8QJmZzwEZ15togEWydepcPet1pJi"
    TutorialHelper.configureTwitterCredentials(apiKey, apiSecret, accessToken, accessTokenSecret)

    // Your code goes here

    val ssc = new StreamingContext(new SparkConf(), Seconds(1))
    val tweets = TwitterUtils.createStream(ssc, None)
    val statuses = tweets.map(status => status.getText())
    //statuses.print()
    val words = statuses.flatMap(status => status.split(" "))
    val hashtags = words.filter(word => word.startsWith("#"))
    val counts = hashtags.map(tag => (tag, 1))
                     .reduceByKeyAndWindow(_ + _, _ - _, Seconds(60 * 5), Seconds(1))
    val sortedCounts = counts.map { case(tag, count) => (count, tag) }
                         .transform(rdd => rdd.sortByKey(false))
sortedCounts.foreach(rdd =>
  println("\nTop 10 hashtags:\n" + rdd.take(10).mkString("\n")))

    //hashtags.print()

    ssc.checkpoint(checkpointDir)
    
    ssc.start()
    ssc.awaitTermination()
  }
}

