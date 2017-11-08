import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;



/**
 * @author Jessa Bekker
 *
 * This class is a stub for a perceptron with count-min sketch
 *
 * (c) 2017
 */
public class PerceptronCountMinSketch extends OnlineTextClassifier{

    private int nbOfHashes;
    private int logNbOfBuckets;
    private double learningRate;
    private double bias;
    private double[][] weights; // weights[h][i]: The h'th weight estimate for n-grams that hash to value i for the h'th hash function
    private int[] seeds;

    /* FILL IN HERE */
    private int tasks;
    private boolean updateflag;
    private LabeledText labeledText;
    private Object updatelock;
    private double updatePred;
    //value for update multi-Thread
    
    private Iterator<String> ite;
    private ParsedText parsedText;
    private Object predictionlock;
    private boolean predflag;
    private double makePred;
    //value for prediction multi-Thread
    /**multi thread process the data when update
     */
    private class updateThread extends Thread{
    	public void run(){
    		while(true)
    		{
    			int k=0;
    			synchronized(updatelock){
    				
    				while( (tasks < 0) || (labeledText==null) )
    				{
    					try {
    						updatelock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}//when the taskes >=0 the thread will just wait
    				k=tasks;
    				tasks--;
    				//take one tasks so the counter--
    			}
    			double y= (labeledText.label==1?1.0:-1.0);
    			//begin to update every weight in k'th hashes
    			for(String str:labeledText.text.ngrams){
    	        	int idex=hash(str,seeds[k]);
    	        	weights[k][idex] += learningRate*(y-updatePred);
    	        	
    	        }
    			if(k==0){
    				synchronized(updatelock){
    					updateflag=false;
    					updatelock.notifyAll();
    				}
    			}
    			
    		}//end while
    	}
    };
    /**multi thread process the data when make prediction
     */
    private class makepredictionThread extends Thread{
    	
    	public void run(){
    		double[] tmp=new double[nbOfHashes];
    		while(true){
    			String str="";
    			boolean hasNext=true;
    			synchronized(predictionlock){
    				while(ite==null || !ite.hasNext()){
    					try {
							predictionlock.wait();
						} catch (InterruptedException e) {
						// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    				str=ite.next();
    				hasNext=ite.hasNext();
    			}
    	        for(int i=0;i<weights.length;i++){
    	        	int idex=hash(str,seeds[i]);
    	        	tmp[i]=weights[i][idex];
    	        }
    	        double tmpweights=0;
    	        if((nbOfHashes%2)==0){
    	        	int k=nbOfHashes/2;
    	        	for(int i=0;i<k+1;i++){
    	        		for(int j=i+1;j<nbOfHashes;j++){
    	        			double swap=0;
    	        			if(tmp[i]<tmp[j]){
    	        				swap=tmp[j];
    	        				tmp[j]=tmp[i];
    	       					tmp[i]=swap;
    	       				}
    	       			}
    	       		}
    	       		tmpweights= (tmp[k-1]+tmp[k])/2;
    	       	}
    	       	else{
    	       		int k=(nbOfHashes-1)/2;
            		for(int i=0;i<k+1;i++){
   	        			for(int j=i+1;j<nbOfHashes;j++){
   	        				double swap=0;
   	        				if(tmp[i]<tmp[j]){
   	        					swap=tmp[j];
   	        					tmp[j]=tmp[i];
    	        				tmp[i]=swap;
    	        			}
    	        		}
    	        	}
    	        	tmpweights= tmp[k];
    	       	}
    	        
   				synchronized(predictionlock){
   					makePred += tmpweights;
   					if(!hasNext){
   						predflag=false;
   						predictionlock.notifyAll();
   					}
   				}
   			
    		}
    	}
    }
    updateThread[] updatethreads;
    makepredictionThread[] predthreads;
    /**
     * Initialize the perceptron classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses the count-min sketch to estimate the weights of the n-grams
     *
     * @param nbOfHashes The number of hash functions in the count-min sketch
     * @param logNbOfBuckets The hash functions hash to the range [0,2^NbOfBuckets-1]
     * @param learningRate The size of the updates of the weights
     * 
     */
    public PerceptronCountMinSketch(int nbOfHashes, int logNbOfBuckets, double learningRate){
        this.nbOfHashes = nbOfHashes;
        this.logNbOfBuckets=logNbOfBuckets;
        this.learningRate = learningRate;
        this.threshold = 0;
        int size=1<<logNbOfBuckets;
        weights = new double[nbOfHashes][size];
        
        bias=1.0;
      //initial the seed
        this.tasks = -1;
        updateflag=false;
        this.updatelock=new Object();
        this.labeledText=null;
        this.updatePred=0.0;
        this.updatethreads = new updateThread[8];
        for(int i=0;i<updatethreads.length;i++){
        	this.updatethreads[i]=new updateThread();
        	this.updatethreads[i].setDaemon(true);
        	this.updatethreads[i].start();
        }
        
        this.predictionlock=new Object();
        this.parsedText=null;
        this.ite=null;
        predflag=false;
        makePred=0.0;
        this.predthreads =new makepredictionThread[8];
        for(int i=0;i<predthreads.length;i++){
        	this.predthreads[i]=new makepredictionThread();
        	this.predthreads[i].setDaemon(true);
        	this.predthreads[i].start();
        }
        
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
        /* FILL IN HERE */
        int size = 0;
        int key = 0;
        size=1<<this.logNbOfBuckets;
        size--;
        key = MurmurHash.hash32(str, seeds[h]);
        v = key & size;
  
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
        updatePred=this.makePrediction(labeledText.text);
        this.labeledText=labeledText;
        synchronized(this.updatelock){
        	
        	this.tasks = this.nbOfHashes-1;
        	this.updateflag=true;
        	this.updatelock.notifyAll();
        	while(updateflag){
        		try {
            		this.updatelock.wait();
            	} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
            	}
        	}
        	this.updateflag=false;
        }

        /*double y= (labeledText.label==1?1.0:-1.0);
        
        for(String str:labeledText.text.ngrams){
        	for(int i=0;i<weights.length;i++){
        		int idex=hash(str,seeds[i]);
        		this.weights[i][idex] += this.learningRate*(y-pred);
        	}
        }
        */
        double y= (labeledText.label==1?1.0:-1.0);
        this.bias +=this.learningRate*(y-updatePred);
        /* FILL IN HERE */

    }

    /**
     * Uses the current model to make a prediction about the incoming e-mail belonging to class "1" (spam)
     * If the prediction is positive, then the e-mail is classified as spam.
     *
     * This method gives the output of the perceptron, before it is passed through the threshold function.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param text is an parsed incoming e-mail
     * @return the prediction
     */
    @Override
    public double makePrediction(ParsedText text) {
        double pr = 0;
        this.makePred=0;
        synchronized(this.predictionlock){
        	this.parsedText = text;
        	this.ite=this.parsedText.ngrams.iterator();
        	
        	if(ite.hasNext())
        		predflag=true;
        	else predflag=false;
        	//make sure we have tasks need to wait
        	
        	this.predictionlock.notifyAll();
        	while(predflag){
        		try {
					this.predictionlock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	this.predflag=false;
        	ite=null;
        }
        /*double[] tmp=new double[this.nbOfHashes];
        for(String str:text.ngrams){
        	for(int i=0;i<weights.length;i++){
        		int idex=hash(str,seeds[i]);
        		tmp[i]=weights[i][idex];
        	}
        	double tmpweights=0;
        	if((this.nbOfHashes%2)==0){
        		int k=this.nbOfHashes/2;
        		for(int i=0;i<k+1;i++){
        			for(int j=i+1;j<this.nbOfHashes;j++){
        				double swap=0;
        				if(tmp[i]<tmp[j]){
        					swap=tmp[j];
        					tmp[j]=tmp[i];
        					tmp[i]=swap;
        				}
        			}
        		}
        		tmpweights= (tmp[k-1]+tmp[k])/2;
        	}
        	else{
        		int k=(this.nbOfHashes-1)/2;
        		for(int i=0;i<k+1;i++){
        			for(int j=i+1;j<this.nbOfHashes;j++){
        				double swap=0;
        				if(tmp[i]<tmp[j]){
        					swap=tmp[j];
        					tmp[j]=tmp[i];
        					tmp[i]=swap;
        				}
        			}
        		}
        		tmpweights= tmp[k];
        	}
        	pr += tmpweights;
        }
        */
        pr = this.makePred+bias;
        
        return pr;
    }



    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {

        if (args.length < 8) {
            System.err.println("Usage: java PerceptronCountMinSketch <indexPath> <stopWordsPath> <logNbOfBuckets> <nbOfHashes> <learningRate> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 8 or 9 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            int nbOfHashes = Integer.parseInt(args[3]);
            double learningRate = Double.parseDouble(args[4]);
            String out = args[5];
            int reportingPeriod = Integer.parseInt(args[6]);
            int n = Integer.parseInt(args[7]);
            boolean writeOutAllPredictions = args.length>8 && args[8].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            PerceptronCountMinSketch perceptron = new PerceptronCountMinSketch(nbOfHashes ,logNbOfBuckets, learningRate);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy(),new Precision(),new Recall(),new F1_score()}; //ADD AT LEAST TWO MORE EVALUATION METRICS
            long begintime = System.currentTimeMillis();
            perceptron.makeLearningCurve(stream, evaluationMetrics, out+".pcms", reportingPeriod, writeOutAllPredictions);
            long endtime = System.currentTimeMillis();
            System.out.println(endtime-begintime);

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
