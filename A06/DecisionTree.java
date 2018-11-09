import java.util.Random;

abstract class Node {
	abstract boolean isLeaf();
	int getAttribute(){return 0;};
	double getPivot(){return 0;};
	Node getA(){return null;};
	Node getB(){return null;};
	double[] getLabels(){return null;};
	public boolean categorical;
}

class InteriorNode extends Node {
	int attribute; // which attribute to divide on
	double pivot; // which value to divide on
	Node a;
	Node b;

	public InteriorNode(Node a, Node b, int att, double piv, boolean cat) {
		this.a = a;
		this.b = b;
		attribute = att;
		pivot = piv;
		categorical = cat;
	}

	boolean isLeaf() {
		return false;
	}

	public int getAttribute() {
		return attribute;
	}

	public double getPivot() {
		return pivot;
	}

	public Node getA() {
		return a;
	}

	public Node getB() {
		return b;
	}
}

class LeafNode extends Node {
	double[] label;

	public LeafNode(Matrix lab, boolean cat) {
		label = new double[lab.cols()];
		for (int i = 0; i < lab.cols(); i++) {
			/*if (!cat)
				label[i] = lab.mostCommonValue(i);
			else*/
				label[i] = lab.mostCommonValue(i);
		}
		categorical = cat;
	}

	public double[] getLabels() {
		return label;
	}

	boolean isLeaf() {
		return true;
	}
}


public class DecisionTree extends SupervisedLearner {
	Node root;
	Random rand;

	public DecisionTree(){
		long seed = (long)(Math.random()*Math.pow(10,10));
		rand = new Random(seed);
		//System.out.println("seed: "+seed);
	}

	public DecisionTree(long seed){
		rand = new Random(seed);
	}

	String name(){
		return "DecisionTree";
	}

	double[] pick_dividing_column_and_pivot(Matrix feat, Matrix lab){
		double[] r = new double[2];
		r[0] = rand.nextInt(feat.cols());
		int row = rand.nextInt(feat.rows());
		r[1] = feat.row(row)[(int)r[0]];
		return r;
	}

	Node build_tree(Matrix feat, Matrix lab) {

		if (feat.rows() != lab.rows()) {
			throw new RuntimeException("mismatched features and labels");
		}

		double[] colpiv = pick_dividing_column_and_pivot(feat, lab);

		int col = (int) colpiv[0];
		double pivot = colpiv[1];

		int vals = feat.valueCount(col);
		boolean cat = (vals!=0);
		Matrix feat_a = new Matrix(0, feat.cols());
		Matrix feat_b = new Matrix(0, feat.cols());
		Matrix lab_a = new Matrix(0, lab.cols());
		Matrix lab_b = new Matrix(0, lab.cols());

		Matrix feat_old = new Matrix(feat);
		Matrix lab_old = new Matrix(lab);

		for(int patience = 0; patience<10; patience++){
			feat = new Matrix(feat_old);
			lab = new Matrix(lab_old);
			feat_a = new Matrix(0, feat.cols());
			feat_b = new Matrix(0, feat.cols());
			lab_a = new Matrix(0, lab.cols());
			lab_b = new Matrix(0, lab.cols());

			colpiv = pick_dividing_column_and_pivot(feat, lab);
			col = (int) colpiv[0];
			pivot = colpiv[1];

			vals = feat.valueCount(col);
			cat = (vals!=0);


			for (int i = 0; i < feat.rows(); i+=0) {
				if (!cat) {
					if (feat.row(i)[col] < pivot) {
						feat_a.takeRow(feat.removeRow(i));
						lab_a.takeRow(lab.removeRow(i));
						/*Vec.copy(feat_a.newRow(), feat.row(i));
						Vec.copy(lab_a.newRow(), lab.row(i));*/
					} else {
						feat_b.takeRow(feat.removeRow(i));
						lab_b.takeRow(lab.removeRow(i));
	//					Vec.copy(feat_b.newRow(), feat.row(i));
	//					Vec.copy(lab_b.newRow(), lab.row(i));
					}
				} else {
					if (feat.row(i)[col] == pivot) {
						feat_a.takeRow(feat.removeRow(i));
						lab_a.takeRow(lab.removeRow(i));
	//					Vec.copy(feat_a.newRow(), feat.row(i));
	//					Vec.copy(lab_a.newRow(), lab.row(i));
					} else {
						feat_b.takeRow(feat.removeRow(i));
						lab_b.takeRow(lab.removeRow(i));
	//					Vec.copy(feat_b.newRow(), feat.row(i));
	//					Vec.copy(lab_b.newRow(), lab.row(i));
					}
				}
			}
			if(feat_a.rows()>0&&feat_b.rows()>0){
				break;
			}

		}

		//System.out.println("a size: "+feat_a.rows()+" b size: "+feat_b.rows());

		if (feat_a.rows() == 0 || feat_b.rows() == 0) {
			if(feat_a.rows()==0)return new LeafNode(lab_b, cat);
			else return new LeafNode(lab_a, cat);
		}

		Node a = build_tree(feat_a, lab_a);
		Node b = build_tree(feat_b, lab_b);

		return new InteriorNode(a, b, col, pivot, cat);

	}

	void train(Matrix feat, Matrix lab) {
		root = build_tree(feat, lab);
	}

	double[] predict(double[] feat) {
		Node n = root;
		while (true) {
			if(!n.categorical) {
				if (!n.isLeaf()) {
					if (feat[n.getAttribute()] < n.getPivot()) {
						n = n.getA();
					} else {
						n = n.getB();
					}
				} else {
					return n.getLabels();
				}
			}
			else{
				if (!n.isLeaf()) {
					if (feat[n.getAttribute()] == n.getPivot()) {
						n = n.getA();
					} else {
						n = n.getB();
					}
				} else {
					return n.getLabels();
				}
			}
		}
	}

}
