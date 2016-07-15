package edu.univalle.qnetsimengine;

/**
 * @author nagel
 */
abstract class QItem {
	
	abstract double getEarliestLinkExitTime();

	abstract void setEarliestLinkExitTime(double earliestLinkEndTime);

}