package data;

/**
 * @author Tianyi Wang
 */
public class DataSplittingInfo {
	public int splittingIndex;
	public double bestInfoGainRatio;
	
	public DataSplittingInfo(int splittingIndex, double bestInfoGainRatio) {
		this.splittingIndex = splittingIndex;
		this.bestInfoGainRatio = bestInfoGainRatio;
	}
}