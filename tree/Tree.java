package tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import data.DataInformationGain;
import data.DataInstance;
import data.DataSplittingInfo;

/**
 * @author Tianyi Wang
 */
public class Tree {

	final static private double GAIN_RATIO_THRESHOLD = 0.01;
	
	abstract private class TreeNode {
		// Nothing here. Meant to be inherited.
	}
	
	private class DecisionTreeNode extends TreeNode {
		private int attributeIndex;
		private HashMap<Integer, TreeNode> attributeValueToChildNode;
		private boolean positiveMajority;
		
		public DecisionTreeNode(int attributeIndex, boolean positiveMajority) {
			this.attributeIndex = attributeIndex;
			this.attributeValueToChildNode = new HashMap<Integer, Tree.TreeNode>();
			this.positiveMajority = positiveMajority;
		}
		
		public void addChildNode(int attributeValue, TreeNode childNode) {
			attributeValueToChildNode.put(attributeValue, childNode);
		}
	}
	
	private class LabelTreeNode extends TreeNode {
		private int label;
		
		public LabelTreeNode(int label) {
			this.label = label;
		}
	}
	
	private TreeNode rootNode;
	
	/*************************** PRIVATE METHODS ***************************/
	
	private double base2Log(double x) {
		// To avoid NaN problem:
		if (x == 0.0) {
			x = Double.MIN_VALUE;
		}
		return Math.log(x) / Math.log(2);
	}
	
	private double getInfoGain(double ratio1, double ratio2) {
		return -ratio1 * base2Log(ratio1) - ratio2 * base2Log(ratio2);
	}
	
	private double getInfoGain(int count1, int count2) {
		double ratio1 = ((double) count1) / (count1 + count2);
		double ratio2 = 1 - ratio1;
		return getInfoGain(ratio1, ratio2);
	}
	
	private double getSplitInfo(int curCount, int totalCount) {
		double division = ((double) curCount) / totalCount;
		return division * base2Log(division);
	}
	
	/**
	 * Select the attribute with the highest information gain (lowest uncertainty/entropy).
	 * 
	 * @param treeData the {@link TreeNode} object.
	 * @param useRandom whether or not to choose the attribute randomly.
	 * @return the {@link DataSplittingInfo} object.
	 */
	private DataSplittingInfo selectAttribute(TreeData treeData, boolean useRandom) {
		int bestAttributeIndex = -1;
		double bestGainRatio = -1;
		
		if (useRandom) {
			// Use all the nodes regardless.
			/*
			int index = random.nextInt(treeData.attributes.size());
			int i = 0;
			for (Integer result : treeData.attributes) {
				if (index == i) {
					return result;
				}
				i++;
			}
			*/
		}

		double info = getInfoGain(treeData.numPositiveClass, treeData.numNegativeClass);
		
		// Now test each one of the attributes.
		Iterator<Integer> itr = treeData.attributes.iterator();
		while (itr.hasNext()) {
			Integer curAttribute = itr.next();
			
			// Try split using this attribute.
			HashMap<Integer, DataInformationGain> attributeValueToIGData = new HashMap<Integer, DataInformationGain>();
			
			// Go through all the data.
			Iterator<DataInstance> dataItr = treeData.data.iterator();
			while (dataItr.hasNext()) {
				DataInstance curDataInstance = dataItr.next();
				int curAttributeValue = curDataInstance.getAttributes().get(curAttribute);
				
				if (attributeValueToIGData.containsKey(curAttributeValue)) {
					if (curDataInstance.getLabel() == +1) {
						attributeValueToIGData.get(curAttributeValue).numPositive++;
					} else {
						attributeValueToIGData.get(curAttributeValue).numNegative++;
					}
				} else {
					DataInformationGain dataInfoGain = new DataInformationGain();
					if (curDataInstance.getLabel() == +1) {
						dataInfoGain.numPositive++;
					} else {
						dataInfoGain.numNegative++;
					}
					attributeValueToIGData.put(curAttributeValue, dataInfoGain);
				}
			}
			
			double infoAttri = 0.0;
			double splitInfo = 0.0;
			double total = treeData.data.size();
			for (DataInformationGain dataInfoGain : attributeValueToIGData.values()) {
				int curTotal = dataInfoGain.numPositive + dataInfoGain.numNegative;
				
				infoAttri += (curTotal / total * getInfoGain(dataInfoGain.numPositive, dataInfoGain.numNegative));
				splitInfo += getSplitInfo(curTotal, (int) total);
			}
			
			double gainAttri = info - infoAttri;
			// Adjust the split info.
			if (splitInfo == 0.0) {
				splitInfo = 1.0;
			} else {
				splitInfo *= -1;
			}
			double gainRatioAttri = gainAttri / splitInfo;

			if (gainRatioAttri > bestGainRatio) {
				bestGainRatio = gainRatioAttri;
				bestAttributeIndex = curAttribute;
			}
		}

		return new DataSplittingInfo(bestAttributeIndex, bestGainRatio);
	}
	
	/**
	 * Generate a Decision Tree based on provided data.
	 * 
	 * @param treeData the {@link TreeData} object.
	 * @param randomlySelectAttri whether or not to randomly select attribute when splitting.
	 * @param treeHeight current height of the tree.
	 * @return the root of the tree.
	 */
	private TreeNode generateDecisionTree(TreeData treeData, boolean randomlySelectAttri, int treeHeight) {
		TreeNode node = null;
		
		if (treeData.numPositiveClass == 0) {
			node = new LabelTreeNode(-1);
			return node;
		}
		else if (treeData.numNegativeClass == 0) {
			node = new LabelTreeNode(+1);
			return node;
		}
		
		boolean positiveMajority = treeData.numPositiveClass > treeData.numNegativeClass ? true : false;
		
		if (treeData.attributes.size() == 0) {
			int label = positiveMajority ? +1 : -1;
			node = new LabelTreeNode(label);
			return node;
		}
		
		DataSplittingInfo splittingInfo = selectAttribute(treeData, randomlySelectAttri);
		int splittingAttributeIndex = splittingInfo.splittingIndex;
		double splittingGainRatio = splittingInfo.bestInfoGainRatio;
		if (splittingAttributeIndex == -1) {
			System.out.println("No splitting attribute was selected, something went wrong.");
			System.out.println(treeData);
			System.exit(1);
		}
		
		// If we are not using random selection, that means we are using Decision Tree.
		// In such case, do pruning. Skip pruning otherwise.
		if (randomlySelectAttri == false) {
			// Prune when the best gain ratio is below the threshold.
			if (splittingGainRatio < GAIN_RATIO_THRESHOLD) {
				int label = positiveMajority ? +1 : -1;
				node = new LabelTreeNode(label);
				return node;
			}
		}
		
		node = new DecisionTreeNode(splittingAttributeIndex, positiveMajority);
		treeData.attributes.remove(splittingAttributeIndex);
		
		HashMap<Integer, TreeData> attributeValueToTreeData = new HashMap<Integer, TreeData>();
		Iterator<DataInstance> itr = treeData.data.iterator();
		while (itr.hasNext()) {
			DataInstance dataInstance = itr.next();
			int attributeValue = dataInstance.getAttributes().get(splittingAttributeIndex);
			
			if (attributeValueToTreeData.containsKey(attributeValue)) {
				TreeData curTreeData = attributeValueToTreeData.get(attributeValue);
				curTreeData.data.add(dataInstance);
				if (dataInstance.getLabel() == +1) {
					curTreeData.numPositiveClass++;
				} else {
					curTreeData.numNegativeClass++;
				}
			}
			else {
				ArrayList<DataInstance> listForThisAttributeValue = new ArrayList<DataInstance>();
				HashSet<Integer> attributes = new HashSet<Integer>(treeData.attributes);
				listForThisAttributeValue.add(dataInstance);
				int numPositive = dataInstance.getLabel() == +1 ? 1 : 0;
				int numNegative = dataInstance.getLabel() == -1 ? 1 : 0;
				TreeData newTreeData = new TreeData(listForThisAttributeValue,
													numPositive,
													numNegative,
													attributes);
				attributeValueToTreeData.put(attributeValue, newTreeData);
			}
		}
		
		Iterator<Entry<Integer, TreeData>> mapItr = attributeValueToTreeData.entrySet().iterator();
		while (mapItr.hasNext()) {
			Entry<Integer, TreeData> entry = mapItr.next();
			Integer attributeValue = entry.getKey();
			TreeData treeDataOfChild = entry.getValue();
			
			TreeNode childNode = generateDecisionTree(treeDataOfChild, randomlySelectAttri, treeHeight + 1);
			
			((DecisionTreeNode)node).addChildNode(attributeValue, childNode);
		}
		
		return node;
	}
	
	private int getLabel(DataInstance dataInstance, TreeNode node) {
		if (node instanceof LabelTreeNode) {
			return ((LabelTreeNode) node).label;
		}
		
		int attributeIndex = ((DecisionTreeNode) node).attributeIndex;
		int dataAttributeValue = dataInstance.getAttributes().get(attributeIndex);
		
		TreeNode childNode = ((DecisionTreeNode) node).attributeValueToChildNode.get(dataAttributeValue);
		if (childNode == null) {
			// There is no data instance that satisfies the required attribute value.
			// Use the current node's majority vote to decide the label.
			return ((DecisionTreeNode) node).positiveMajority ? +1 : -1;
		}
		
		return getLabel(dataInstance, childNode);
	}
	
	/*************************** PUBLIC METHODS ***************************/

	/**
	 * Construct the Decision Tree using C4.5.
	 * 
	 * @param treeData data used to construct this Decision Tree.
	 * @param randomlySelectAttri whether or not to randomly select attribute when splitting.
	 */
	public Tree (TreeData treeData, boolean randomlySelectAttri) {		
		// Generate the entire tree with all the tree data.
		this.rootNode = generateDecisionTree(treeData, randomlySelectAttri, 0);
	}
	
	public int getLabel(DataInstance dataInstance) {
		return getLabel(dataInstance, rootNode);
	}
}