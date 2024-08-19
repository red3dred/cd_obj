package com.invasion.entity;

public interface IHasAiGoals {
    Goal getAIGoal();

    Goal getPrevAIGoal();

    Goal transitionAIGoal(Goal newGoal);

    default boolean hasGoal(Goal goal) {
        return getAIGoal() == goal;
    }

    default boolean hasAnyGoal(Goal... goals) {
        for (Goal goal : goals) {
            if (hasGoal(goal)) {
                return true;
            }
        }
        return false;
    }

    default boolean isBetweenGoals(Goal a, Goal b) {
        return getPrevAIGoal() == a && getAIGoal() == b;
    }

    default boolean hasOrIsBetweenGoals(Goal a, Goal b) {
        return getAIGoal() == a || isBetweenGoals(a, b);
    }

    public enum Goal {
      TARGET_ENTITY,
      GOTO_ENTITY,
      NONE,
      BREAK_NEXUS,
      CHILL,
      FLYING_TARGET_ENTITY,
      STAY_AT_RANGE,
      FIND_ATTACK_OPPORTUNITY,
      SWOOP,
      FLYING_STRIKE,
      SWITCH_TARGET,
      PICK_UP_TARGET,
      TACKLE_TARGET,
      MELEE_TARGET,
      LEAVE_MELEE,
      STABILISE
    }
}
