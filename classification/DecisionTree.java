package classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import tree.Tree;
import tree.TreeData;

import data.DataInstance;

/**
 * @author Tianyi Wang
 */
public class DecisionTree {
	
	private static Tree decisionTree = null;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("DecisionTree train_file test_file");
			return;
		}
		
		String trainingFileName = args[0];
		String testingFileName = args[1];
		
		BufferedReader trainingBufferReader = null;
		BufferedReader testingBufferReader = null;
		try {
			trainingBufferReader = new BufferedReader(new FileReader(trainingFileName));
			testingBufferReader = new BufferedReader(new FileReader(testingFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Start to train.
		processTrainingData(trainingBufferReader);
		
		try {
			trainingBufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Start to evaluate.
		processTestingData(testingBufferReader);
		
		try {
			testingBufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read in data and convert/store them in customized data structure.
	 * 
	 * @param bufferReader {@link BufferedReader} for reading the data file.
	 * @return a customized data structure {@link TreeData}.
	 */
	public static TreeData processDataGeneral(BufferedReader bufferReader) {
		ArrayList<DataInstance> result = new ArrayList<DataInstance>();
		int numPositiveClass = 0;
		int numNegativeClass = 0;
		
		String line;
		
		try {
			while ((line = bufferReader.readLine()) != null) {
				DataInstance dataInstance = new DataInstance(line);
				result.add(dataInstance);
				
				if (dataInstance.getLabel() == 1) {
					numPositiveClass++;
				} else {
					numNegativeClass++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Create attributes list.
		int attributesNum = result.get(0).getAttributes().size();
		HashSet<Integer> attributes = new HashSet<Integer>(attributesNum);
		for (int i = 0; i < attributesNum; i++) {
			attributes.add(i);
		}
		
		return new TreeData(result, numPositiveClass, numNegativeClass, attributes);
	}
	
	/**
	 * Build the classifier with the training data.
	 * 
	 * @param trainingBufferReader {@link BufferedReader} for reading training data.
	 */
	private static void processTrainingData(BufferedReader trainingBufferReader) {
		TreeData trainingTreeData = processDataGeneral(trainingBufferReader);
		decisionTree = new Tree(trainingTreeData, false); 
	}
	
	/**
	 * Evaluate the classifier with the testing data.
	 * 
	 * @param testingBufferReader {@link BufferedReader} for reading testing data.
	 */
	private static void processTestingData(BufferedReader testingBufferReader) {
		if (decisionTree == null) {
			System.out.println("Decision Tree has not been constructed!");
			return;
		}
		
		TreeData testingTreeData = processDataGeneral(testingBufferReader);
		
		int numTP = 0;
		int numFN = 0;
		int numFP = 0;
		int numTN = 0;
		
		Iterator<DataInstance> itr = testingTreeData.data.iterator();
		while (itr.hasNext()) {
			DataInstance dataInstance = itr.next();
			int predictedLabel = decisionTree.getLabel(dataInstance);
			int realLabel = dataInstance.getLabel();
			
			if (predictedLabel == realLabel && predictedLabel == +1) {
				numTP++;
			} else if (predictedLabel == realLabel && predictedLabel == -1) {
				numTN++;
			} else if (predictedLabel != realLabel && predictedLabel == +1) {
				numFP++;
			} else if (predictedLabel != realLabel && predictedLabel == -1) {
				numFN++;
			}
		}
		
		// Output the quality evaluation:
		// true positive, false negative, false positive, and true negative.
		System.out.println(numTP);
		System.out.println(numFN);
		System.out.println(numFP);
		System.out.println(numTN);
	}
}