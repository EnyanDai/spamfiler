
public class Precision implements EvaluationMetric{

	@Override
	public double evaluate(int TP, int FP, int TN, int FN) {
		double PredPos =(double)TP+(double)FP;
		return (PredPos!=0)?((double)TP/PredPos):0.0;
	}

	@Override
	public String name() {
		return "Precision";
	}
	

}
