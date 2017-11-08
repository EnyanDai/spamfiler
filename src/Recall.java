
public class Recall implements EvaluationMetric {

	@Override
	public double evaluate(int TP, int FP, int TN, int FN) {
		double TruePos=(double)TP+(double)FN;
		return (TruePos!=0)?((double)TP/TruePos):0.0;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "Recall";
	}

}
