package forest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import data.DataInstance;

import tree.Tree;
import tree.TreeData;

/**
 * @author Tianyi Wang
 */
public class Forest {
	final private static int FOREST_K = 50;
	
	private ArrayList<Tree> forest = null;
	
	private Random random;

	/*************************** PRIVATE METHODS ***************************/

	/**
	 * Generate the Random Forest (RI).
	 * 
	 * @param treeData the data used to generate all the trees.
	 * @param numTrees the number of trees to generate for this forest.
	 */
	private void generateRandomForest(TreeData treeData, int numTrees) {
		int treeSize = treeData.data.size();
		
		for (int i = 0; i < numTrees; i++) {
			// Sample the data.
			ArrayList<DataInstance> data = new ArrayList<DataInstance>();
			int numPositive = 0;
			int numNegative = 0;
			for (int j = 0; j < treeSize; j++) {
				DataInstance temp = treeData.data.get(random.nextInt(treeSize));
				data.add(temp);
				if (temp.getLabel() == +1) {
					numPositive++;
				} else {
					numNegative++;
				}
			}
			
			// Build the new TreeData using the sampled data.
			TreeData sampleTreeData = new TreeData(data, numPositive, numNegative,
												new HashSet<Integer>(treeData.attributes));
			
			Tree curTree = new Tree(sampleTreeData, true);
			forest.add(curTree);
		}
	}
	
	/*************************** PUBLIC METHODS ***************************/

	public Forest(TreeData treeData) {
		this.random = new Random();

		forest = new ArrayList<Tree>(FOREST_K);

		generateRandomForest(treeData, FOREST_K);
	}
	
	public int getLabel(DataInstance dataInstance) {
		HashMap<Integer, Integer> labelToCount = new HashMap<Integer, Integer>();
		
		Iterator<Tree> itr = forest.iterator();
		while (itr.hasNext()) {
			Tree curTree = itr.next();
			
			int curLabel = curTree.getLabel(dataInstance);
			
			if (labelToCount.containsKey(curLabel)) {
				int oldCount = labelToCount.get(curLabel);
				oldCount++;
				labelToCount.put(curLabel, oldCount);
			} else {
				labelToCount.put(curLabel, 1);
			}
		}
		
		int maxCountLabel = -1;
		int maxCount = -1;
		for (Map.Entry<Integer, Integer> entry : labelToCount.entrySet()) {
			Integer label = entry.getKey();
			Integer count = entry.getValue();
			
			if (count > maxCount) {
				maxCountLabel = label;
				maxCount = count;
			}
		}

		return maxCountLabel;
	}
}