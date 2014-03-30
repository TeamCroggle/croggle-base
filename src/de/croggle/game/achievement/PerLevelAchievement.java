package de.croggle.game.achievement;

import de.croggle.data.persistence.Statistic;

/**
 * Achievement for passing certain, specified goals within a level, e.g. placing
 * more than 10 Alligators within one level or 5 eggs hatched within one level.
 */
public abstract class PerLevelAchievement extends Achievement {

	@Override
	public int requirementsMet(Statistic statistic, Statistic statisticDelta) {
		if (statisticDelta.getLevelsComplete() > 0) {
			return requirementsMet(statisticDelta);
		}
		return getIndex();
	}
	
	abstract int requirementsMet(Statistic statisticDelta);

}
