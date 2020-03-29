public class SearchAlgorithm {

  // Your search algorithm should return a solution in the form of a valid
  // schedule before the deadline given (deadline is given by system time in ms)
  public Schedule backtrackAlg(SchedulingProblem problem, long deadline) {

    // get an empty solution to start from
    Schedule solution = problem.getEmptySchedule();
    boolean[] assignment = new boolean[problem.courses.size()];
    int[] courseConstraints = genContraintsList(problem);

    solution = recursiveBacktracking(assignment, problem, solution, courseConstraints);

    return solution;
  }

  public Schedule recursiveBacktracking(boolean[] assignment, SchedulingProblem problem, Schedule solution, int[] courseConstraints) {
    if(isComplete(assignment)) return solution;

    Course c = leastConstrainingCourse(assignment, problem, courseConstraints);
    boolean preferredEnabled = true;

    for(int i = 0; i < c.timeSlotValues.length; i ++) {
      if(c.timeSlotValues[i] > 0) {
        for(int j = 0; j < problem.rooms.size(); j++) {
          Room r = problem.rooms.get(j);
          if(c.preferredLocation == r.b && preferredEnabled) {
            if(solution.schedule[j][i] < 0) {
              solution.schedule[j][i] = problem.courses.indexOf(c);
              assignment[problem.courses.indexOf(c)] = true;
              return recursiveBacktracking(assignment, problem, solution, courseConstraints);
            }
          } else if(!preferredEnabled) {
            if (solution.schedule[j][i] < 0) {
              solution.schedule[j][i] = problem.courses.indexOf(c);
              assignment[problem.courses.indexOf(c)] = true;
              return recursiveBacktracking(assignment, problem, solution, courseConstraints);
            }
          } 
        }
      }
      if (i == c.timeSlotValues.length - 1 && !assignment[problem.courses.indexOf(c)] && preferredEnabled){
        preferredEnabled = false;
        i = (i+1) % c.timeSlotValues.length;
      } else if(i == c.timeSlotValues.length - 1 && !preferredEnabled) {
        return solution;
      }
    }
    return solution;
  }

  Course leastConstrainingCourse(boolean[] assignment, SchedulingProblem problem, int[] courseConstraints) {
    Course leastConstrainingCourse = null;
    int remainingConstraints = 0;

    for(int i = 0; i < problem.courses.size(); i++) {
      Course c = problem.courses.get(i);

      if(assignment[i]) continue;

      int totalConstraints = remainingConstraints(courseConstraints, c, problem);

      if(remainingConstraints < totalConstraints) {
        remainingConstraints = totalConstraints;
        leastConstrainingCourse = c;
      }
    }
    
    updateConstraints(courseConstraints, leastConstrainingCourse, problem);

    return leastConstrainingCourse;
  }

  int remainingConstraints(int[] constraints, Course selectedCourse, SchedulingProblem problem) {
    int sumConstraints = 0;

    for (int i = 0; i < problem.courses.size(); i++) {
      Course currentCourse = problem.courses.get(i);
      for (int j = 0; j < selectedCourse.timeSlotValues.length; j++) {
        if (selectedCourse.timeSlotValues[j] > 0 && currentCourse.timeSlotValues[j] > 0) {
          sumConstraints++;
        }
      }
    }

    return sumConstraints;
  }

  void updateConstraints (int[]constraints, Course selectedCourse, SchedulingProblem problem) {
    for(int i = 0; i < problem.courses.size(); i++) {
      Course currentCourse = problem.courses.get(i);
      for(int j = 0; j < selectedCourse.timeSlotValues.length; j++) {
        if(selectedCourse.timeSlotValues[j] > 0 && currentCourse.timeSlotValues[j] > 0) {
          constraints[i]--;
        }
      }
    }
  }

  int[] genContraintsList(SchedulingProblem problem) {
    int[] courseConstraints = new int[problem.courses.size()];

    for(int i = 0; i < courseConstraints.length; i++) {
      courseConstraints[i] = 1;
      Course c = problem.courses.get(i);
      for(int j = 0; j < c.timeSlotValues.length; j++) {
        if(c.timeSlotValues[j] > 0)
          courseConstraints[i] += 1;
      }
    }
    return courseConstraints;
  }

  boolean isComplete(boolean[] assignment) {
    for(int i = 0; i < assignment.length; i++) {
      if (assignment[i] == false)
        return false;
    }
    return true;
  }

  Course selectUnassignedCourse(boolean[] assignment, SchedulingProblem problem) {
    for(int i = 0; i < assignment.length; i++) {
      if(assignment[i] == false)
        return problem.courses.get(i);
    }
    return null;
  }

  // This is a very naive baseline scheduling strategy
  // It should be easily beaten by any reasonable strategy
  public Schedule naiveBaseline(SchedulingProblem problem, long deadline) {

    // get an empty solution to start from
    Schedule solution = problem.getEmptySchedule();

    for (int i = 0; i < problem.courses.size(); i++) {
      Course c = problem.courses.get(i);
      boolean scheduled = false;
      for (int j = 0; j < c.timeSlotValues.length; j++) {
        if (scheduled) break;
        if (c.timeSlotValues[j] > 0) {
          for (int k = 0; k < problem.rooms.size(); k++) {
            if (solution.schedule[k][j] < 0) {
              solution.schedule[k][j] = i;
              scheduled = true;
              break;
            }
          }
        }
      }
    }

    return solution;
  }
}
