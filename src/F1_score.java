
public class F1_score implements EvaluationMetric {

	@Override
	public double evaluate(int TP, int FP, int TN, int FN) {
		Precision P=new Precision();
		Recall R=new Recall();
		double p=P.evaluate(TP, FP, TN, FN);
		double r=R.evaluate(TP, FP, TN, FN);
		double tmp=p+r;
		return (tmp!=0)?((2*p*r)/(tmp)):0.0;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "F1 Score";
	}

}
