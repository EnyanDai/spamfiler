import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

/**
 * @author Jessa Bekker
 *
 * This class is a stub for naive Bayes with count-min sketch
 *
 * (c) 2017
 */
public class NaiveBayesCountMinSketch extends OnlineTextClassifier{

    private int nbOfHashes;
    private int logNbOfBuckets;
    private int[][][] counts; // counts[c][h][i]: The count of n-grams in e-mails of class c (spam: c=1)
                              // that hash to value i for the h'th hash function.
    private int[] classCounts; //classCounts[c] the count of e-mails of class c (spam: c=1)
    
    /* FILL IN HERE */
    private int[] seeds;
    private int tasks;
    private LabeledText labeledText;
    
    private class updateThread extends Thread{
    	public void run(){
    		while(true)
    		{
    			int k=0;
    			synchronized(NaiveBayesCountMinSketch.this){
    				
    				while( (NaiveBayesCountMinSketch.this.tasks < 0) || (NaiveBayesCountMinSketch.this.labeledText==null) )
    				{
    					try {
    						NaiveBayesCountMinSketch.this.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}//when the taskes >=0 the thread will just wait
    				
    				k=NaiveBayesCountMinSketch.this.tasks;
    				NaiveBayesCountMinSketch.this.tasks--;
    				//take one tasks so the counter--
    			}
    			
    			//begin to update every counters in k'th hashes
    			for(String str:NaiveBayesCountMinSketch.this.labeledText.text.ngrams)
    			{
    	        	counts[labeledText.label][k][hash(str,k)]++;
    	        }
    			if(k==0){
    				synchronized(NaiveBayesCountMinSketch.this){
    					NaiveBayesCountMinSketch.this.notifyAll();
    				}
    			}
    			
    		}//end while
    	}
    };
    updateThread[] updatethreads;
    /**
     * Initialize the naive Bayes classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses the count-min sketch to estimate the conditional counts of the n-grams
     *
     * @param nbOfHashes The number of hash functions in the count-min sketch
     * @param logNbOfBuckets The hash functions hash to the range [0,2^NbOfBuckets-1]
     * @param threshold The threshold for classifying something as positive (spam). Classify as spam if Pr(Spam|n-grams)>threshold)
     */
    public NaiveBayesCountMinSketch(int nbOfHashes, int logNbOfBuckets, double threshold){
        this.nbOfHashes = nbOfHashes;
        this.logNbOfBuckets=logNbOfBuckets;
        this.threshold = threshold;
        this.tasks = 0;
        this.labeledText=null;
        this.updatethreads = new updateThread[8];
        for(int i=0;i<updatethreads.length;i++){
        	this.updatethreads[i]=new updateThread();
        	this.updatethreads[i].setDaemon(true);
        	this.updatethreads[i].start();
        }
        
        int size=1<<logNbOfBuckets;
        counts=new int[2][nbOfHashes][size];
        for(int i=0;i<counts.length;i++){
        	for(int j=0;j<counts[i].length;j++){
        		for(int k=0;k<counts[i][j].length;k++){
        			counts[i][j][k]=1;
        		}
        	}
        }
        classCounts=new int[]{0,0};
        
        seeds=new int[nbOfHashes];
        for(int i=0;i<seeds.length;i++){
        	seeds[i]=i;
        }
        /* FILL IN HERE */

    }

    /**
     * Calculate the hash value of the h'th hash function for string str
     *
     * THIS METHOD IS REQUIRED
     *
     * The hash function hashes to the range [0,2^NbOfBuckets-1]
     * This method should work for h in the range [0, nbOfHashes-1]
     *
     * @param str The string to calculate the hash function for
     * @param h The number of the hash function to use.
     * @return the hash value of the h'th hash function for string str
     */
    private int hash(String str, int h){
        int v=0;
        int size = 0;
        int key = 0;
        size=1<<this.logNbOfBuckets;
        size--;
        key = MurmurHash.hash32(str, seeds[h]);
        v = key & size;
        /* FILL IN HERE */

        return v;
    }

    /**
     * This method will update the parameters of your model using the incoming mail.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param labeledText is an incoming e-mail with a spam/ham label
     */
    @Override
    public void update(LabeledText labeledText){
        super.update(labeledText);
        this.classCounts[labeledText.label]++;
        
        /*for(String str:labeledText.text.ngrams){
        	for(int i=0;i<this.nbOfHashes;i++){
        		counts[labeledText.label][i][hash(str,i)]++;
        	}
        }*/
        synchronized(this){
        	this.labeledText=labeledText;
        	this.tasks = this.nbOfHashes-1;
        	this.notifyAll();
        	while(this.tasks > 0 ){
        		try {
            		this.wait();
            	} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
            	}
        	}
        }
        /* FILL IN HERE */

    }


    /**
     * Uses the current model to make a prediction about the incoming e-mail belonging to class "1" (spam)
     * The prediction is the probability for the e-mail to be spam.
     * If the probability is larger than the threshold, then the e-mail is classified as spam.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param text is an parsed incoming e-mail
     * @return the prediction
     */
    @Override
    public double makePrediction(ParsedText text) {
        double pr = 0.0;

        /* FILL IN HERE */
        double logHam = 0;
        double logSpam =0;
        
        for(String str:text.ngrams){
        	int HamCount=this.counts[0][0][hash(str,0)];
        	int SpamCount=this.counts[1][0][hash(str,0)];
        	
        	for(int i=1;i<this.nbOfHashes;i++){
        		int idex=hash(str,i);
        		int tmpHamCount=this.counts[0][i][idex];
        		HamCount=HamCount<tmpHamCount?HamCount:tmpHamCount;
        		int tmpSpamCount=this.counts[1][i][idex];
        		SpamCount=SpamCount<tmpSpamCount?SpamCount:tmpSpamCount;
        	}//find the min count O(h)
        	
        	logHam += Math.log( HamCount ) - Math.log( (this.classCounts[0] + 2.0 ) );
        	logSpam += Math.log( SpamCount ) - Math.log( (this.classCounts[1] + 2.0 ) );
        }
        logHam += Math.log(this.classCounts[0]) - Math.log(this.nbExamplesProcessed);
        logSpam += Math.log(this.classCounts[1]) - Math.log(this.nbExamplesProcessed);
        //finish log 
        double logAll = 0;
        logAll -= Math.log(1.0+Math.exp(logHam-logSpam));
        pr = Math.exp(logAll);
        return pr;
    }


    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 8) {
            System.err.println("Usage: java NaiveBayesCountMinSketch <indexPath> <stopWordsPath> <logNbOfBuckets> <nbOfHashes> <threshold> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 8 or 9 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            int nbOfHashes = Integer.parseInt(args[3]);
            double threshold = Double.parseDouble(args[4]);
            String out = args[5];
            int reportingPeriod = Integer.parseInt(args[6]);
            int n = Integer.parseInt(args[7]);
            boolean writeOutAllPredictions = args.length>8 && args[8].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            NaiveBayesCountMinSketch nb = new NaiveBayesCountMinSketch(nbOfHashes ,logNbOfBuckets, threshold);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy()}; //ADD AT LEAST TWO MORE EVALUATION METRICS
            long begintime = System.currentTimeMillis();
            nb.makeLearningCurve(stream, evaluationMetrics, out+".nbcms", reportingPeriod, writeOutAllPredictions);
            long endtime = System.currentTimeMillis();
            System.out.println(endtime-begintime);

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }
}
