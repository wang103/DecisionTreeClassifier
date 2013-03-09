package classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import data.DataInstance;

import forest.Forest;

import tree.TreeData;

/**
 * @author Tianyi Wang
 */
public class RandomForest {

	private static Forest forest;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("RandomForest train_file test_file");
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
	 * Build the forest with the training data.
	 * 
	 * @param trainingBufferReader {@link BufferedReader} for reading training data.
	 */
	private static void processTrainingData(BufferedReader trainingBufferReader) {
		TreeData trainingTreeData = DecisionTree.processDataGeneral(trainingBufferReader);
		
		// Build the forest.
		forest = new Forest(trainingTreeData);
	}
	
	/**
	 * Evaluate the forest with the testing data.
	 * 
	 * @param testingBufferReader {@link BufferedReader} for reading testing data.
	 */
	private static void processTestingData(BufferedReader testingBufferReader) {
		if (forest == null) {
			System.out.println("Random Forest has not been constructed!");
			return;
		}
		
		TreeData testingTreeData = DecisionTree.processDataGeneral(testingBufferReader);
		
		int numTP = 0;
		int numFN = 0;
		int numFP = 0;
		int numTN = 0;
		
		Iterator<DataInstance> itr = testingTreeData.data.iterator();
		while (itr.hasNext()) {
			DataInstance dataInstance = itr.next();
			int predictedLabel = forest.getLabel(dataInstance);
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