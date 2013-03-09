package tree;

import java.util.ArrayList;
import java.util.HashSet;

import data.DataInstance;

/**
 * @author Tianyi Wang
 */
public class TreeData {
	public ArrayList<DataInstance> data;
	public int numPositiveClass;
	public int numNegativeClass;
	public HashSet<Integer> attributes;
	
	public TreeData(ArrayList<DataInstance> data, int numPositiveClass, int numNegativeClass,
					HashSet<Integer> attributes) {
		this.data = data;
		this.numPositiveClass = numPositiveClass;
		this.numNegativeClass = numNegativeClass;
		this.attributes = attributes;
	}

	/**
	 * For debugging purpose.
	 */
	@Override
	public String toString() {
		return "TreeData [data=" + data + ", numPositiveClass="
				+ numPositiveClass + ", numNegativeClass=" + numNegativeClass
				+ ", attributes=" + attributes + "]";
	}	
}