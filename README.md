# Tweets sentiment classifier group project

###Project description
This project classifies tweets with specified hashtag into positive and negative ones. It's implemented using 
Scala language and Apache Spark framework and includes several stages:
* **Streaming** - converting RSS stream of tweets from QueryFeed into Spark data abstraction - RDD (ready GitHub project was used for that)
* **Preprocessing** - remove redundant characters and words, convert to lowercase, etc.
* **Model training** -  using models from Spark Machine Learning library and training them on Twitter Sentiment Analysis dataset.
* **Classifying tweets** - using saved ML model for classifying tweets from RSS stream.

###Project structure
* ```classifier/``` — Classification module
    * ```Main``` - example class of how to operate with Model class (also generated pretrained model)
    * ```MLFindModel``` - used to identify the model with best accuracy
    * ```Model``` - class which represents the classifier
    * ```ModelLoader``` - simple support static methods
* ```preprocessing/``` — Preprocessing module
    * ```PreprocessingDataset``` - static method for preprocessing train dataset before learning
    * ```PreprocessingExample``` - obvious from the name
    * ```PreprocessTweet``` - class for processing text messages (described earlier)
* ```streamer/``` - streaming module

###How to use a project
* If you wish to train model - use example provided in ```classifier.Main```
* If you wish to start streaming rss & process it - run ```streamer.RSSDemo``` with one argument - tag (cat, dog, winter, etc.). Just one word, nothing else.

###Outputs
* Pretrained model is then stored close to ```classifier/twits/train.csv``` resource (in model folder). 
* Each new batch produced by RSS generates two artifacts - folder with initial data and after model process. ```(${tag}_input and ${tag}_result)```

###Configurations
* All the manipulations with spark were done on local machine without usage of the cluster. 
* Paths in a program are configured to use files from local machine.
* RDDs are generated each 10 seconds.

### References
[QueryFeed] (http://queryfeed.net/)
[RSS streaming GitHub repository] (https://github.com/CatalystCode/streaming-rss-html)
[Twitter Sentiment Analysis] (https://www.kaggle.com/c/twitter-sentiment-analysis2/data)