##
## Makefile
##
## Jessa Bekker
## Oct 2017
## 

SMALL_DATA=../dataSubset/index_small

DATA=/cw/bdap/assignment1/Data/index

STOPWORDS=../stop-word-list_stanford.txt

MAX_N=2
LOG_NB_BUCKETS=20
NB_HASHES=10
THRESHOLD=0.5
LEARNING_RATE=0.0001


# Experiment parameters
OUT=out
PERIOD=1000

SMALL_OUT=small
SMALL_PERIOD=100


all: NaiveBayesFeatureHashing.class NaiveBayesCountMinSketch.class PerceptronFeatureHashing.class PerceptronCountMinSketch.class

NaiveBayesFeatureHashing.class: NaiveBayesFeatureHashing.java
	javac -cp .:lib/* $<
	
NaiveBayesCountMinSketch.class: NaiveBayesCountMinSketch.java
	javac -cp .:lib/* $<
	
PerceptronFeatureHashing.class: PerceptronFeatureHashing.java
	javac -cp .:lib/* $<
	
PerceptronCountMinSketch.class: PerceptronCountMinSketch.java
	javac -cp .:lib/* $<
	
#TODO: Add compilation commands for other subclasses of OnlineTextClassifier (optional)
	

nbfh_small: NaiveBayesFeatureHashing.class
	@echo "Testing naive Bayes with feature hashing on a subset of the data"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

nbfh: NaiveBayesFeatureHashing.class
	@echo "Testing naive Bayes with feature hashing on the complete data"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD) $(OUT) $(PERIOD) $(MAX_N)

nbcms_small: NaiveBayesCountMinSketch.class
	@echo "Testing naive Bayes with count-min sketch on a subset of the data"
	rm -f $(SMALL_OUT).nbcms.*
	time java -cp .:lib/* NaiveBayesCountMinSketch $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(THRESHOLD) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

nbcms: NaiveBayesCountMinSketch.class
	@echo "Testing naive Bayes with count-min sketch on the complete data"
	rm -f $(OUT).nbcms.*
	time java -cp .:lib/* NaiveBayesCountMinSketch $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(THRESHOLD) $(OUT) $(PERIOD) $(MAX_N)

pfh_small: PerceptronFeatureHashing.class
	@echo "Testing perceptron classification with feature hashing on a subset of the data"
	rm -f $(SMALL_OUT).pfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(LEARNING_RATE) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

pfh: PerceptronFeatureHashing.class
	@echo "Testing perceptron classification with feature hashing on the complete data"
	rm -f $(OUT).pfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(LEARNING_RATE) $(OUT) $(PERIOD) $(MAX_N)

pcms_small: PerceptronCountMinSketch.class
	@echo "Testing perceptron classification with count-min sketch on a subset of the data"
	rm -f $(SMALL_OUT).pcms.*
	time java -cp .:lib/* PerceptronCountMinSketch $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(LEARNING_RATE) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

pcms: PerceptronCountMinSketch.class
	@echo "Testing perceptron classification with count-min sketch on the complete data"
	rm -f $(OUT).pcms.*
	time java -cp .:lib/* PerceptronCountMinSketch $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(LEARNING_RATE) $(OUT) $(PERIOD) $(MAX_N)



	
clean:
	rm *.class
