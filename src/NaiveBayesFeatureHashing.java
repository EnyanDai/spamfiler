import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.math.*;
/**
 * @author Jessa Bekker
 *
 * This class is a stub for naive bayes with feature hashing
 *
 * (c) 2017
 */
public class NaiveBayesFeatureHashing extends OnlineTextClassifier{

    private int logNbOfBuckets;
    private int[][] counts; // counts[c][i]: The count of n-grams in e-mails of class c (spam: c=1) that hash to value i
    private int[] classCounts; //classCounts[c] the count of e-mails of class c (spam: c=1)
    final int SPAM = 1;
    final int HAM = 0;
    /* FILL IN HERE */

    /**
     * Initialize the naive Bayes classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses simple feature hashing: The features of this classifier are the hash values that n-grams
     * hash to.
     *
     * @param logNbOfBuckets The hash function hashes to the range [0,2^NbOfBuckets-1]
     * @param threshold The threshold for classifying something as positive (spam). Classify as spam if Pr(Spam|n-grams)>threshold)
     */
    public NaiveBayesFeatureHashing(int logNbOfBuckets, double threshold){
        this.logNbOfBuckets=logNbOfBuckets;
        this.threshold = threshold;
        int size=1<<logNbOfBuckets;
        this.counts=new int[2][size];
        for(int i=0;i<counts.length;i++){
        	for(int j=0;j<counts[i].length;j++){
        		counts[i][j]=1;
        	}
        }
        /* initialize the counter with smooth */
        this.classCounts=new int[] {0,0};
        
    }

    /**
     * Calculate the hash value for string str
     *
     * THIS METHOD IS REQUIRED
     *
     * The hash function hashes to the range [0,2^NbOfBuckets-1]
     *
     * @param str The string to calculate the hash function for
     * @return the hash value of the h'th hash function for string str
     */
    private int hash(String str){
        int v = 0;
        int seed = 17;
        int size = 0;
        int key = 0;
        size=1<<(this.logNbOfBuckets);
        size--;
        //System.out.println(Integer.toBinaryString(size));
        key = MurmurHash.hash32(str, seed);
        //System.out.println(Integer.toBinaryString(key));
        v = key & size;
        //System.out.println(Integer.toBinaryString(v));
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
        for(String str:labeledText.text.ngrams){
        	counts[labeledText.label][hash(str)]++;
        }

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
        double pr = 0;

        /* FILL IN HERE */
        double logHam = 0;
        double logSpam =0;
        for(String str:text.ngrams){
        	int idex=hash(str);
        	logHam += Math.log( this.counts[0][idex] ) - Math.log( (this.classCounts[0] + 2.0 ) );
        	logSpam += Math.log( this.counts[1][idex] ) - Math.log( (this.classCounts[1] + 2.0 ) );
        }
        logHam += Math.log(this.classCounts[0]) - Math.log(this.nbExamplesProcessed);
        logSpam += Math.log(this.classCounts[1]) - Math.log(this.nbExamplesProcessed);
        double logAll = 0;
        logAll -= Math.log(1.0+Math.exp(logHam-logSpam));
        pr=Math.exp(logAll);
        return pr;
    }



    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println("Usage: java NaiveBayesFeatureHashing <indexPath> <stopWordsPath> <logNbOfBuckets> <threshold> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 7 or 8 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            double threshold = Double.parseDouble(args[3]);
            String out = args[4];
            int reportingPeriod = Integer.parseInt(args[5]);
            int n = Integer.parseInt(args[6]);
            boolean writeOutAllPredictions = args.length>7 && args[7].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            NaiveBayesFeatureHashing nb = new NaiveBayesFeatureHashing(logNbOfBuckets, threshold);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy()}; //ADD AT LEAST TWO MORE EVALUATION METRICS
            nb.makeLearningCurve(stream, evaluationMetrics, out+".nbfh", reportingPeriod, writeOutAllPredictions);

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
