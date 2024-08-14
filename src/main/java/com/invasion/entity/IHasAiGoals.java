package com.invasion.entity;

import com.invasion.entity.ai.Goal;

public interface IHasAiGoals {
    Goal getAIGoal();

    Goal getPrevAIGoal();

    Goal transitionAIGoal(Goal newGoal);

    default boolean hasGoal(Goal goal) {
        return getAIGoal() == goal;
    }

    default boolean isBetweenGoals(Goal a, Goal b) {
        return getPrevAIGoal() == a && getAIGoal() == b;
    }

    default boolean hasOrIsBetweenGoals(Goal a, Goal b) {
        return getAIGoal() == a || isBetweenGoals(a, b);
    }

}
