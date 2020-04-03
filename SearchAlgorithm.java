import java.util.*;

public class SearchAlgorithm {

  public Schedule simulatedAnnealingAlg(SchedulingProblem problem, long deadline) {
    Schedule solution = problem.getEmptySchedule();
    boolean[] assignment = new boolean[problem.courses.size()];

    solution = iterativeSA(problem, solution, assignment, deadline);

    return solution;
  }

  public Schedule iterativeSA(SchedulingProblem problem, Schedule currentSolution, boolean[] assignment, long deadline) {
    int time = 100;
    double tmax = 5000;
    double tmin = 10;
    double coolingRate = 0.5;

    currentSolution = naiveBaseline(problem, deadline);

    for(int i = 0; i < time; i++) {
      if(i % 10 == 0) tmax *= coolingRate;
      if(tmax <= tmin) return currentSolution;
      Schedule nextSolution = move(currentSolution, assignment, problem);
      double eCurrent = problem.evaluateSchedule(currentSolution);
      double eNext = problem.evaluateSchedule(nextSolution);
      double deltaE =  eNext - eCurrent;
      if(deltaE > 0) currentSolution =  nextSolution;
      else{
        if(probabilityMet(deltaE,tmax)) currentSolution = nextSolution;
      }
      System.out.println(i);
    }
    return currentSolution;
  }

  Schedule move(Schedule currentSolution, boolean[] assignment, SchedulingProblem problem) {
    Schedule newSolution = createCopy(currentSolution, problem);

    int firstCourse = -1;
    int secondCourse = -1;
    int firstTimeSlot = -1;
    int secondTimeSlot = -1;

    for(int i = 0; i < newSolution.schedule.length; i++) {        // Rows = Rooms
      for(int j = 0; j < newSolution.schedule[i].length; ++j) {   // Columns = Timeslots
        if (firstCourse == -1 && newSolution.schedule[i][j] > -1) {
          firstCourse = newSolution.schedule[i][j];
          firstTimeSlot = j;
        } 
        if(secondCourse == -1 && newSolution.schedule[i][j] > -1) {
          secondCourse = newSolution.schedule[i][j];
          secondTimeSlot = j;
        }

        if(firstCourse > -1 && secondCourse > -1) {
          double firstBonus = problem.courses.get(firstCourse).timeSlotValues[firstTimeSlot];
          double secondBonus = problem.courses.get(secondCourse).timeSlotValues[secondTimeSlot];
          double actualBonus = firstBonus + secondBonus;

          double newFBonus = problem.courses.get(firstCourse).timeSlotValues[secondTimeSlot];
          double newSBonus = problem.courses.get(secondCourse).timeSlotValues[firstTimeSlot];
          double newBonus = newFBonus + newSBonus;

          if(actualBonus < newBonus) {
            newSolution.schedule[i][firstTimeSlot] = secondCourse;
            newSolution.schedule[i][secondTimeSlot] = firstCourse;
            return newSolution;
          } else {
            firstCourse = -1;
            secondCourse = -1;
            firstTimeSlot = -1;
            secondTimeSlot = -1;
          }
        }
      }
    }

    return newSolution;
  }

  Schedule createCopy(Schedule current, SchedulingProblem problem) {
    Schedule copy = problem.getEmptySchedule();

    for(int i = 0; i < current.schedule.length; i++) {
      for(int j = 0; j < current.schedule[i].length; j++) {
        copy.schedule[i][j] = current.schedule[i][j];
      }
    }

    return copy;
  }

  boolean probabilityMet(double deltaE, double t) {
    Random random = new Random();
    double prob = random.nextDouble() * 1;
    double power = deltaE / t;

    return Math.exp(power) > prob ? true : false;
  }

  Schedule initialSchedule(SchedulingProblem problem, Schedule solution, boolean[] assignment) {
    Random random = new Random();

    int courseIndex = random.nextInt(problem.courses.size());;
    Course c = problem.courses.get(courseIndex);

    for(int i = 0; i < c.timeSlotValues.length; i++){
      for(int j = 0; j < problem.rooms.size(); j++) {
        if (solution.schedule[j][i] < 0) {
            while(!assignment[courseIndex]) {
              courseIndex = random.nextInt(problem.courses.size());
              c = problem.courses.get(courseIndex);
              System.out.println("I am here!");
            }
            solution.schedule[j][i] = courseIndex;
            assignment[courseIndex] = true;
        }
      }
    }

    return solution;
  }

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
