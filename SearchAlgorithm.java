
/**
 * Authors: Miguel A. Zamudio & Cristian Ayub
 * Class: 4320/5314
 * Instructor: Dr. Christopher Kiekintveld
 * Last updated: 04/04/2020
 */

import java.util.*;

public class SearchAlgorithm {

  /**
   * This method allows to run simulated annealing algorithm given
   * a problem and a deadline
   * 
   * @param problem
   * @param deadline
   * @return
   */
  public Schedule simulatedAnnealingAlg(SchedulingProblem problem, long deadline) {
    Schedule solution = problem.getEmptySchedule();
    boolean[] assignment = new boolean[problem.courses.size()];

    solution = iterativeSA(problem, solution, assignment, deadline);

    return solution;
  }

  /**
   * This method implements simulated annealing algorithm
   * given a problem, a schedule, a list of already assigned courses, 
   * and a deadline
   * 
   * @param problem
   * @param currentSolution
   * @param assignment
   * @param deadline
   * @return
   */
  public Schedule iterativeSA(SchedulingProblem problem, Schedule currentSolution, boolean[] assignment, long deadline) {
    int time = 100;
    double tmax = getTemperature(currentSolution);
    double tmin = 0;
    double coolingRate = 0.3;

    // currentSolution = naiveBaseline(problem, deadline);
    // currentSolution = initialSchedule(problem, currentSolution);
    currentSolution = backtrackAlg(problem, deadline);

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
    }
    return currentSolution;
  }

  /**
   * This method calculates the cooling rate for simulated annealing
   * depending on the size of the given schedule
   * 
   * @param schedule
   * @return
   */
  double getCoolingRate(Schedule schedule) {
    double initialRate = 0.3;

    int size = schedule.schedule.length;

    if (size < 100) {
      initialRate += 0;
    }
    if (size < 1000) {
      initialRate += 0.1;
    }
    if (size < 10000) {
      initialRate += 0.1;
    }

    return initialRate;
  }

  /**
   * This method calculates the temperature depending on the 
   * size of the given schedule
   * 
   * @param schedule
   * @return
   */
  double getTemperature(Schedule schedule) {
    double t = 1.0;

    int size = schedule.schedule.length;

    if(size < 100) {
      t = size * 10.0;
    }
    if(size < 1000) {
      t = size * 3.0;
    }
    if(size < 10000) {
      t = size * 0.5;
    }

    return t;
  }

  /**
   * This method creates a new solution given a current solution, a list of already 
   * assigned courses, and a problem. This new solution is a 'move' for the 
   * simulated annealing algorithm
   * 
   * @param currentSolution
   * @param assignment
   * @param problem
   * @return
   */
  Schedule move(Schedule currentSolution, boolean[] assignment, SchedulingProblem problem) {
    Schedule newSolution = createCopy(currentSolution, problem);
    int[][] s = newSolution.schedule;
    Random random = new Random();

    int firstX = random.nextInt(s.length);
    int firstY = random.nextInt(s[0].length);
    int secondX = random.nextInt(s.length);
    int secondY = random.nextInt(s[0].length);

    int temp = s[firstX][firstY];
    s[firstX][firstY] = s[secondX][secondY];
    s[secondX][secondY] =  temp;

    return newSolution;
  }

  /**
   * This method creates a copy of a schedule given a problem
   * 
   * @param current
   * @param problem
   * @return
   */
  Schedule createCopy(Schedule current, SchedulingProblem problem) {
    Schedule copy = problem.getEmptySchedule();

    for(int i = 0; i < current.schedule.length; i++) {
      for(int j = 0; j < current.schedule[i].length; j++) {
        copy.schedule[i][j] = current.schedule[i][j];
      }
    }

    return copy;
  }

  /**
   * This method checks if exp probability is met given
   * a delta energy and some temperature
   * 
   * @param deltaE
   * @param t
   */
  boolean probabilityMet(double deltaE, double t) {
    Random random = new Random();
    double prob = random.nextDouble() * 1;
    double power = deltaE / t;

    return Math.exp(power) > prob ? true : false;
  }

  /**
   * This algorithm generates an initial random solution for simulated 
   * annealing algorithm.
   * 
   * @param problem
   * @param solution
   * @return
   */
  Schedule initialSchedule(SchedulingProblem problem, Schedule solution) {
    Random random = new Random();

    for(int i = 0; i < problem.courses.size(); i++) {
      int randomTimeSlot = random.nextInt(problem.courses.get(i).timeSlotValues.length);
      int randomRoom =  random.nextInt(problem.rooms.size());

      solution.schedule[randomRoom][randomTimeSlot] = i;
    }

    return solution;
  }

  /**
   * This method allows to implement backtracking algorithm given a problem
   * and a deadline
   * 
   * @param problem
   * @param deadline
   * @return
   */
  public Schedule backtrackAlg(SchedulingProblem problem, long deadline) {

    Schedule solution = problem.getEmptySchedule();
    boolean[] assignment = new boolean[problem.courses.size()];
    int[] courseConstraints = genContraintsList(problem);

    solution = recursiveBacktracking(assignment, problem, solution, courseConstraints);

    return solution;
  }

  /**
   * This method implements backtracking search algorithm using least
   * constraining value as heuristic. This algorithm requires a problem, a list of 
   * already assigned courses, a schedule, and a list of constrains for each course.
   * 
   * @param assignment
   * @param problem
   * @param solution
   * @param courseConstraints
   */
  public Schedule recursiveBacktracking(boolean[] assignment, SchedulingProblem problem, Schedule solution, int[] courseConstraints) {
    if(isComplete(assignment)) return solution;

    Course c = leastConstrainingCourse(assignment, problem, courseConstraints);
    // Course c =  selectUnassignedCourse(assignment, problem);
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

  /**
   * This method returns the least constraining course given a problem,
   * a list of constraints of all courses, and a list of which courses have been
   * already assigned
   * 
   * @param assignment
   * @param problemn
   * @param courseConstraints
   */
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

  /**
   * This method sums up all the constraints available in the list of 
   * constraints and returns such a number
   * 
   * @param constraints
   * @param selectedCourse
   * @param problem
   * @return
   */
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

  /**
   * This method updates the list of constraints for every course depending 
   * on the selected course and the given problem
   * 
   * @param constraints
   * @param selectedCourse
   * @param problem
   */
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

/**
 * Given a problem this method generates a list counting the 
 * constraints for each course
 * 
 * @param problem
 * @return
 */
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

  /**
   * This method checks whether the list of assigned courses is
   * completed
   * 
   * @param assignment
   * @return
   */
  boolean isComplete(boolean[] assignment) {
    for(int i = 0; i < assignment.length; i++) {
      if (assignment[i] == false)
        return false;
    }
    return true;
  }

  /**
   * This method selects the first unassigned course and returns it given
   * a problem and the list of already assigned courses
   * 
   * @param assignment
   * @param problem
   * @return
   */
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
