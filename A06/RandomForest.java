import java.util.ArrayList;
import java.util.Random;

public class RandomForest extends SupervisedLearner {

	int size;
	Random rand;
	ArrayList<DecisionTree> trees = new ArrayList<>();

	public RandomForest(int num){
		size=num;
		long seed = (long)(Math.random()*Math.pow(10,10));
		rand = new Random(seed);
		//System.out.println("seed: "+seed);
	}

	public RandomForest(int num, long seed){
		size=num;
		rand = new Random(seed);
	}



	public String name(){
		return "RandomForest";
	}

	void train(Matrix feat, Matrix lab) {
		trees.clear();
		if (feat.rows() != lab.rows()) {
			throw new RuntimeException("mismatched features and labels");
		}

		for(int i=0; i<size; i++){
			Matrix sample_feat = new Matrix(0, feat.cols());
			Matrix sample_lab = new Matrix(0, lab.cols());

			for(int j=0; j<feat.rows(); j++){
				int row = rand.nextInt(feat.rows());
				Vec.copy(sample_feat.newRow(), feat.row(row));
				Vec.copy(sample_lab.newRow(), lab.row(row));
			}

			DecisionTree tree = new DecisionTree();
			tree.train(sample_feat, sample_lab);
			trees.add(tree);
		}
	}

	double[] predict(double[] feat) {
		Matrix votes = new Matrix(0, trees.get(0).predict(feat).length);
		for(DecisionTree tree : trees){
			votes.takeRow(tree.predict(feat));
		}
		double[] prediction = new double[votes.cols()];
		for(int i=0; i<votes.cols(); i++){
			prediction[i] = votes.mostCommonValue(i);
		}
		return prediction;
	}
}
