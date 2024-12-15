import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import javax.swing.JOptionPane;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class PSS {
	public static Scanner scanner = new Scanner(System.in);
	public static ArrayList<Task> listOfTasks = new ArrayList<>();
	
	public PSS() {
        listOfTasks = new ArrayList<>();
    }
	
	public ArrayList<Task> getListOfTasks(){
		return listOfTasks;
	}

    // check for overlaps with existing tasks (name), returns true if there is overlap
    public static boolean checkForNameOverlap(String newName) {
        for (Task task : listOfTasks) {
            if (task.Name.equals(newName)) {
                // name overlap detected
                return true;
            }
        }
            return false; // no overlap detected
    }
	public boolean checkDateRange(RecurringTask task, int date)
    	{
        	// int -> String (for comparisons)
        	String targetDate = String.valueOf(date);
        	String startDate = String.valueOf(task.StartDate);
        	String endDate = String.valueOf(task.EndDate);
        
        	// true == date is within range
        	return targetDate.compareTo(startDate) >= 0 && targetDate.compareTo(endDate) <= 0;
    	}

 // check for overlaps with existing tasks (date and time) returns true if there is overlap
    public boolean checkforDateTimeOverlap(int newDate, float newStartTime, float newDuration) {
        for (Task task : listOfTasks) {
            if (task.Date == newDate) {
                // Check if the task is a RecurringTask
                if (task instanceof RecurringTask) {
                    RecurringTask recurringTask = (RecurringTask) task;
                    // Iterate over all occurrence dates and check for overlaps
                    List<Integer> occurrenceDates = recurringTask.getAllOccurrenceDates();
                    for (int occurrenceDate : occurrenceDates) {
                        // Check for overlap only if there's no anti-task for this occurrence date
                        if (!hasAntiTaskForDateTime(occurrenceDate, recurringTask.StartTime)) {
                            float taskStartTime = recurringTask.StartTime;
                            float taskEndTime = taskStartTime + recurringTask.Duration;
                            float newEndTime = newStartTime + newDuration;
                            if ((newStartTime >= taskStartTime && newStartTime < taskEndTime) ||
                                (newEndTime > taskStartTime && newEndTime <= taskEndTime)) {
                                // overlap detected based on date and time
                                return true;
                            }
                        }
                    }
                } else {
                    // For non-recurring tasks, check for overlap directly
                    float taskEndTime = task.StartTime + task.Duration;
                    float newEndTime = newStartTime + newDuration;
                    if ((newStartTime >= task.StartTime && newStartTime < taskEndTime) ||
                        (newEndTime > task.StartTime && newEndTime <= taskEndTime)) {
                        // overlap detected based on date and time
                        return true;
                    }
                }
            }
		else if (task instanceof RecurringTask)
            	{
                	RecurringTask recurringTask = (RecurringTask) task;
                	if (checkDateRange(recurringTask, newDate))
                	{
                	    return true;
                	}
            	}
        }
        return false; // no overlap detected
    }

    // Helper method to check if there's an anti-task for a specific date
    public static boolean hasAntiTaskForDateTime(int date, float startTime) {
        for (Task task : listOfTasks) {
            if (task instanceof AntiTask && task.Date == date && task.StartTime == startTime) {
                return true;
            }
        }
        return false;
    }


    public static Task getTaskDateTimeOverlap(int newDate, float newStartTime, float newDuration) {
        for (Task task : listOfTasks) {
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;
                for (int occurrenceDate : recurringTask.getAllOccurrenceDates()) {
                    // Check if the occurrence date matches the new date
                    if (occurrenceDate == newDate) {
                        // Check if the start time overlaps with the recurring task's start time
                        if (newStartTime >= recurringTask.StartTime && newStartTime < (recurringTask.StartTime + recurringTask.Duration)) {
                            // Found an occurrence of the recurring task at the same date and time
                            return recurringTask;
                        }
                    }
                }
            } else {
                // Check for overlap with non-recurring tasks
                float taskEndTime = task.StartTime + task.Duration;
                float newEndTime = newStartTime + newDuration;
                if (task.Date == newDate) {
                	if ((newStartTime >= task.StartTime && newStartTime < taskEndTime) || 
                            (newEndTime > task.StartTime && newEndTime <= taskEndTime)) {
                            // overlap detected based on date and time
                            return task;
                        }
                }
            }
        }
        return null; // no overlap detected
    }


//Create a new task. Also check to make sure it can be made	
    public void createTask(String name, String type, int date, float startTime, float duration, int endDate, int frequency) {
        // Check if the name is unique
        if (checkForNameOverlap(name)) {
            JOptionPane.showMessageDialog(null, "The Name is not unique. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create task object
        Task task;

        // Check if the type is valid
        if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("study") || type.equalsIgnoreCase("sleep") ||
                type.equalsIgnoreCase("exercise") || type.equalsIgnoreCase("work") || type.equalsIgnoreCase("meal")) {
            // Recurring task
            if (!checkforDateTimeOverlap(date, startTime, duration)) {
                task = new RecurringTask(name, type, date, startTime, duration, endDate, frequency);
            } else {
                JOptionPane.showMessageDialog(null, "The Date and Time Overlapped another task. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (type.equalsIgnoreCase("visit") || type.equalsIgnoreCase("shopping") || type.equalsIgnoreCase("appointment")) {
            // Transient task
            if (!checkforDateTimeOverlap(date, startTime, duration)) {
            	//if there is no overlap we can make the task
                task = new TransientTask(name, type, date, startTime, duration);
            } 
            
            else {
            	//if there is overlap we have to check if there is a recurring task with an antiTask
            	Task overlappingTask = getTaskDateTimeOverlap(date, startTime, duration);
                if (overlappingTask instanceof RecurringTask && hasAntiTaskForDateTime(date, startTime)) {
                    //The overlapping task is Recurring, but it has an AntiTask so we can create the task
                	task = new TransientTask(name, type, date, startTime, duration);
                } 
                else if (overlappingTask instanceof AntiTask){
                	//The overlapping task is an AntiTask which means we can create the task
                    task = new TransientTask(name, type, date, startTime, duration);
                }
                else {
                	//the overlapping task is transient or recurring with no antitask. Cannot create
                	JOptionPane.showMessageDialog(null, "There exits an overlapping task. Cannot create this task.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } else if (type.equalsIgnoreCase("cancellation")) {
            // Anti-task
            // Check for overlap with a recurring task
            Task recurringTask = getTaskDateTimeOverlap(date, startTime, duration);
            if (recurringTask != null && recurringTask instanceof RecurringTask) {
                // Check if there's already an anti-task for the recurring task on the given date
                Task antiTask = getAntiTaskForRecurringTask(date, startTime);
                if (antiTask == null) {
                    task = new AntiTask(name, type, date, recurringTask.StartTime, duration);
                } else {
                    JOptionPane.showMessageDialog(null, "An Anti-task already exists for this occurrence of the recurring task. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "No matching recurring task found. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Invalid type
            JOptionPane.showMessageDialog(null, "Invalid task type. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add task to the list
        listOfTasks.add(task);
        sortTasksByDateAndTime(listOfTasks);
    }

    // Helper method to check if there's an anti-task for a recurring task on the given date
    private Task getAntiTaskForRecurringTask(int date, float startTime) {
        for (Task task : listOfTasks) {
            if (task instanceof AntiTask && task.Date == date && task.StartTime == startTime) {
                return task;
            }
        }
        return null;
    }
    
    //Overloaded method for tasks that have no frequency/end date
    public void createTask(String name, String type, int date, float startTime, float duration) {
    	createTask(name, type, date, startTime, duration, 0, 0);
    }

    // delete task
    public void deleteTask(Task task) {
        // check if task is recurring, if it is delete the anti-tasks associated with it as well
        if (task instanceof RecurringTask) {
            // get associated anti-tasks
            List<Integer> occurrencesDates = ((RecurringTask) task).getAllOccurrenceDates();
            for (Integer date : occurrencesDates) {
                Task antiTask = getAntiTaskForRecurringTask(date, task.StartTime);
                // remove anti-tasks
                if (antiTask != null) {
                    listOfTasks.remove(antiTask);
                }
            }
            // remove recurring task
            listOfTasks.remove(task);
        }
        // check if task is anti-task, if deleting results in conflict between other tasks then it will not be deleted
        else if (task instanceof AntiTask) {
            // find the recurring task associated with the anti task
            RecurringTask associatedRecurringTask = null;
            for (Task checkTask : listOfTasks) {
                if (checkTask instanceof RecurringTask) {
                    RecurringTask recurringTask = (RecurringTask) checkTask;
                    List<Integer> occurrencesDates = recurringTask.getAllOccurrenceDates();
                    if (occurrencesDates.contains(task.Date) && recurringTask.StartTime == task.StartTime) {
                        associatedRecurringTask = recurringTask;
                        break;
                    }
                }
            }
            // check if recurring task exists
            if (associatedRecurringTask != null) {
                // check for conflict
                for (Task checkTask : listOfTasks) {
                    if (checkTask != associatedRecurringTask && checkTask != task) {
                        if (checkTask.Date == task.Date && checkTask.StartTime == task.StartTime) {
                            JOptionPane.showMessageDialog(null, "Deleting this Anti-Task would cause conflicts between tasks.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }
        listOfTasks.remove(task);
        sortTasksByDateAndTime(listOfTasks);
    }

    // edit task
    public void editTask(Task task, String name, String type, int date, float startTime, float duration, int endDate, int frequency) {
        // Check if the name is unique
        if (!task.Name.equals(name) && checkForNameOverlap(name)) {
            JOptionPane.showMessageDialog(null, "The Name is not unique. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new task object
        Task newTask;

        // Check if the type is valid
        if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("study") || type.equalsIgnoreCase("sleep") ||
                type.equalsIgnoreCase("exercise") || type.equalsIgnoreCase("work") || type.equalsIgnoreCase("meal")) {
            // Recurring task
            if (!checkforDateTimeOverlap(date, startTime, duration)) {
                newTask = new RecurringTask(name, type, date, startTime, duration, endDate, frequency);
            } else {
                JOptionPane.showMessageDialog(null, "The Date and Time Overlapped another task. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } 
        else if (type.equalsIgnoreCase("visit") || type.equalsIgnoreCase("shopping") || type.equalsIgnoreCase("appointment")) {
         // Transient task
            if (!checkforDateTimeOverlap(date, startTime, duration)) {
            	//if there is no overlap we can make the task
                newTask = new TransientTask(name, type, date, startTime, duration);
            } 
            else {
            	//if there is overlap we have to check if there is a recurring task with an antiTask
            	Task overlappingTask = getTaskDateTimeOverlap(date, startTime, duration);
                if (overlappingTask instanceof RecurringTask && hasAntiTaskForDateTime(date, startTime)) {
                    //The overlapping task is Recurring, but it has an AntiTask so we can create the task
                	newTask = new TransientTask(name, type, date, startTime, duration);
                } 
                else if (overlappingTask instanceof AntiTask){
                	//The overlapping task is an AntiTask which means we can create the task
                    newTask = new TransientTask(name, type, date, startTime, duration);
                }
                else {
                	//the overlapping task is transient or recurring with no antitask. Cannot create
                	JOptionPane.showMessageDialog(null, "There exits an overlapping task. Cannot create this task.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } 
        else if (type.equalsIgnoreCase("cancellation")) {
            // Anti-task
            // Check for overlap with a recurring task
            Task recurringTask = getTaskDateTimeOverlap(date, startTime, duration);
            if (recurringTask != null && recurringTask instanceof RecurringTask) {
                // Check if there's already an anti-task for the recurring task on the given date
                Task antiTask = getAntiTaskForRecurringTask(date, startTime);
                if (antiTask == null) {
                    newTask = new AntiTask(name, type, date, recurringTask.StartTime, duration);
                } else {
                    JOptionPane.showMessageDialog(null, "An Anti-task already exists for this occurrence of the recurring task. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "No matching recurring task found. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Invalid type
            JOptionPane.showMessageDialog(null, "Invalid task type. Did not save task", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (task instanceof AntiTask) {
            // find the recurring task associated with the anti task
            RecurringTask associatedRecurringTask = null;
            for (Task checkTask : listOfTasks) {
                if (checkTask instanceof RecurringTask) {
                    RecurringTask recurringTask = (RecurringTask) checkTask;
                    List<Integer> occurrencesDates = recurringTask.getAllOccurrenceDates();
                    if (occurrencesDates.contains(task.Date) && recurringTask.StartTime == task.StartTime) {
                        associatedRecurringTask = recurringTask;
                        break;
                    }
                }
            }
        
            // check if recurring task exists
            if (associatedRecurringTask != null) {
                // check for conflict
                for (Task checkTask : listOfTasks) {
                    if (checkTask != associatedRecurringTask && checkTask != task) {
                        if (checkTask.Date == task.Date && checkTask.StartTime == task.StartTime) {
                            JOptionPane.showMessageDialog(null, "Editing this Anti-Task would cause conflicts between tasks.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }

        // Update the task fields
        task.Name = newTask.Name;
        task.Type = newTask.Type;
        task.Date = newTask.Date;
        task.StartTime = newTask.StartTime;
        task.Duration = newTask.Duration;

        // Remove the old task from the list
        listOfTasks.remove(task);

        // Add the new task to the list
        listOfTasks.add(newTask);
        sortTasksByDateAndTime(listOfTasks);
    }


    public void editTask(Task task, String name, String type, int date, float startTime, float duration) {
        // Overloaded method without endDate and frequency
        editTask(task, name, type, date, startTime, duration, 0, 0);
    }


    public Task getTaskByName(String name) {
        for (Task task : listOfTasks) {
            if (task.Name.equals(name)) {
                return task;
            }
        }
        return null;
    }
    
    public void readScheduleFromFile(File file, boolean addToCurrentSchedule) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            ArrayList<Task> tasksFromFile = new ArrayList<>();

            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String type = jsonObject.get("Type").getAsString();
                switch (type.toLowerCase()) {
                    case "class":
                    case "study":
                    case "sleep":
                    case "exercise":
                    case "work":
                    case "meal":
                        tasksFromFile.add(gson.fromJson(jsonObject, RecurringTask.class));
                        break;
                    case "visit":
                    case "shopping":
                    case "appointment":
                        tasksFromFile.add(gson.fromJson(jsonObject, TransientTask.class));
                        break;
                    case "cancellation":
                        tasksFromFile.add(gson.fromJson(jsonObject, AntiTask.class));
                        break;
                    default:
                    	JOptionPane.showMessageDialog(null, "Error: Invalid type in Schedule. Did not update schedule.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
            }

            // Validate and add or replace tasks
//          boolean isValid = validateAndAddTasks(tasksFromFile, addToCurrentSchedule);
            
            boolean isValid = true;
            if (!isValid) {
                JOptionPane.showMessageDialog(null, "Error: Invalid tasks found in the file. Did not update schedule.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (addToCurrentSchedule) {
                	listOfTasks.addAll(tasksFromFile);
                    JOptionPane.showMessageDialog(null, "Added schedule successfully", "Success", JOptionPane.PLAIN_MESSAGE);
                } else {
                	listOfTasks = tasksFromFile;
                    JOptionPane.showMessageDialog(null, "Loaded new schedule successfully", "Success", JOptionPane.PLAIN_MESSAGE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Attempt to validate input
    private boolean validateAndAddTasks(ArrayList<Task> tasksFromFile, boolean addToCurrentSchedule) {
    	boolean allTasksValid = true;

    	//Check names
        for (Task newTask : tasksFromFile) {
            boolean isUniqueName = true;
            // Check if the name of the new task is unique among tasks from the file
            for (Task existingTask : tasksFromFile) {
                if (existingTask != newTask && newTask.getName().equals(existingTask.getName())) {
                    isUniqueName = false;
                    break;
                }
            }
            //if we are adding it to the listOfTasks, check list of tasks names too
            if (addToCurrentSchedule) {
            	for (Task existingTask : listOfTasks) {
                    if (existingTask != newTask && newTask.getName().equals(existingTask.getName())) {
                        isUniqueName = false;
                        break;
                    }
                }
            }
            // If the name is not unique, set allTasksValid to false and break the loop
            if (!isUniqueName) {
                allTasksValid = false;
                break;
            }
            
        }
        
        //Check dates
        //Date List to hold all dates 
        List<Integer> allDates = new ArrayList<>();
        for (Task newTask : tasksFromFile) {
        	if (newTask instanceof RecurringTask) {
        		RecurringTask recurringTask = (RecurringTask) newTask;
        		allDates.addAll(recurringTask.getAllOccurrenceDates());
        	}
        	else {
        		allDates.add(newTask.Date);
        	}
        }
        //if we are adding it, get all the dates of the listOfTasks
        if (addToCurrentSchedule) {
        	for (Task newTask : listOfTasks) {
            	if (newTask instanceof RecurringTask) {
            		RecurringTask recurringTask = (RecurringTask) newTask;
            		allDates.addAll(recurringTask.getAllOccurrenceDates());
            	}
            	else {
            		allDates.add(newTask.Date);
            	}
            }
        }
        //Check to see if there are duplicates
        List<Integer> duplicateDates = new ArrayList<>();
        for (int i = 0; i < allDates.size(); i++) {
            for (int j = i + 1; j < allDates.size(); j++) {
                if (allDates.get(i).equals(allDates.get(j))) {
                    duplicateDates.add(allDates.get(i));
                    break;
                }
            }
        }
        
     // List to hold tasks with duplicate dates
        List<Task> tasksWithDuplicateDates = new ArrayList<>();

        while (!duplicateDates.isEmpty()) {
        	int initialDuplicateDatesSize = duplicateDates.size();
        	// Iterate through tasks and check if any task has a date that matches a duplicate date
            for (Task task : tasksFromFile) {
                if (task instanceof RecurringTask) {
                    RecurringTask recurringTask = (RecurringTask) task;
                    for (int date : recurringTask.getAllOccurrenceDates()) {
                        if (duplicateDates.contains(date)) {
                            tasksWithDuplicateDates.add(task);
                        }
                    }
                } else {
                    if (duplicateDates.contains(task.getDate())) {
                        tasksWithDuplicateDates.add(task);
                    }
                }
            }
            if (addToCurrentSchedule) {
            	for (Task task : listOfTasks) {
                    if (task instanceof RecurringTask) {
                        RecurringTask recurringTask = (RecurringTask) task;
                        for (int date : recurringTask.getAllOccurrenceDates()) {
                            if (duplicateDates.contains(date)) {
                                tasksWithDuplicateDates.add(task);
                                break;
                            }
                        }
                    } else {
                        if (duplicateDates.contains(task.getDate())) {
                            tasksWithDuplicateDates.add(task);
                        }
                    }
                }
            }
	        while (!tasksWithDuplicateDates.isEmpty()) {
	        	int initialDuplicateTaskSize = tasksWithDuplicateDates.size();
	        	for (Task task : tasksWithDuplicateDates) {
	        		if (task instanceof RecurringTask) {
	                    RecurringTask recurringTask = (RecurringTask) task;
	                    for (Task newTask : tasksWithDuplicateDates) {
	                    	if (newTask instanceof AntiTask) {
	                    		if(recurringTask.getAllOccurrenceDates().contains(newTask.Date) && recurringTask.StartTime == newTask.StartTime) {
	                    			duplicateDates.remove(newTask.Date);
	                    			tasksWithDuplicateDates.remove(recurringTask);
	                    			tasksWithDuplicateDates.remove(newTask);
	                    		}
	                    	}
	                    }
	        		}
	        	}
	        	if (initialDuplicateTaskSize == tasksWithDuplicateDates.size()) {
	        		JOptionPane.showMessageDialog(null, "There are duplicate Dates. Could not load file", "Error", JOptionPane.ERROR_MESSAGE);
	        		return false;
	        	}
	        	
	        	else if (tasksWithDuplicateDates.size() == 1) {
	        		break;
	        	}
	        	
	        }
	        if (duplicateDates.size() == initialDuplicateDatesSize) {
	        	JOptionPane.showMessageDialog(null, "There are duplicate Dates. Could not load file", "Error", JOptionPane.ERROR_MESSAGE);
        		return false;
	        }
        }
       
        
        // If all tasks have unique names, proceed with adding them to the list
        if (allTasksValid) {
            // If addToCurrentSchedule is true, add tasks to listOfTasks; otherwise, replace listOfTasks with tasksFromFile
            if (addToCurrentSchedule) {
                listOfTasks.addAll(tasksFromFile);
            } else {
                listOfTasks = tasksFromFile;
            }
        }

        return allTasksValid;
    }




 // Method to view schedule for a particular day, week, or month
    public ArrayList<Task> viewSchedule(int startDate, String duration) {
        ArrayList<Task> filteredTasks = new ArrayList<>();
        int endDate = calculateEndDate(startDate, duration);

        for (Task task : listOfTasks) {
            // Skip anti-tasks
            if (task instanceof AntiTask) {
                continue;
            }

            // Check if the task falls within the specified time period
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;
                for (int date : recurringTask.getAllOccurrenceDates()) {
                    if (date >= startDate && date <= endDate) {
                        // Check if there is an anti-task for this occurrence
                        boolean hasAntiTask = false;
                        for (Task antiTask : listOfTasks) {
                            if (antiTask instanceof AntiTask && antiTask.Date == date) {
                                hasAntiTask = true;
                                break;
                            }
                        }
                        // If there's no anti-task, add the occurrence to the list
                        if (!hasAntiTask) {
                            Task occurrenceTask = new Task(task.Name, task.Type, date, task.StartTime, task.Duration);
                            filteredTasks.add(occurrenceTask);
                        }
                    }
                }
            } else if (isTaskWithinTimePeriod(task, startDate, endDate)) {
                // For non-recurring tasks, directly add if they fall within the time period
                filteredTasks.add(task);
            }
        }

        // Sort the filtered tasks by date and time
        sortTasksByDateAndTime(filteredTasks);

        return filteredTasks;
    }


    
 // Method to check if a task falls within the specified time period
    private boolean isTaskWithinTimePeriod(Task task, int startDate, int endDate) {
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            // Check if any occurrence of the recurring task falls within the time period
            for (int date : recurringTask.getAllOccurrenceDates()) {
                if (date >= startDate && date <= endDate) {
                    return true;
                }
            }
            return false;
        } else {
            // For non-recurring tasks, check if the date falls within the time period
            return (task.Date >= startDate && task.Date <= endDate);
        }
    }


    // Method to calculate end date based on start date and duration
    private int calculateEndDate(int startDate, String duration) {
        int year = startDate / 10000;
        int month = (startDate / 100) % 100;
        int day = startDate % 100;

        switch (duration.toLowerCase()) {
            case "day":
                return startDate;
            case "week":
                return incrementDateByDays(startDate, 7);
            case "month":
                if (month == 12) {
                    year++;
                    month = 1;
                } else {
                    month++;
                }
                return year * 10000 + month * 100 + day;
            default:
                return startDate;
        }
    }
    
    private int incrementDateByDays(int date, int days) {
        int year = date / 10000;
        int month = (date / 100) % 100;
        int day = date % 100;

        // Convert the date to days since a reference date
        int daysSinceReference = year * 365 + (year / 4) - (year / 100) + (year / 400);
        for (int m = 1; m < month; m++) {
            daysSinceReference += getDaysInMonth(m, year);
        }
        daysSinceReference += day;

        // Add the specified number of days
        daysSinceReference += days;

        // Convert days back to year, month, and day
        int newYear = 1;
        while (daysSinceReference > 365) {
            int daysInYear = isLeapYear(newYear) ? 366 : 365;
            if (daysSinceReference <= daysInYear) {
                break;
            }
            daysSinceReference -= daysInYear;
            newYear++;
        }
        int newMonth = 1;
        while (daysSinceReference > getDaysInMonth(newMonth, newYear)) {
            daysSinceReference -= getDaysInMonth(newMonth, newYear);
            newMonth++;
        }
        int newDay = daysSinceReference;

        // Combine year, month, and day to form the new date
        int newDate = newYear * 10000 + newMonth * 100 + newDay;
        return newDate;
    }

  // Helper method to get the number of days in a month
    public int getDaysInMonth(int month, int year) {
        if (month == 2) {
            return isLeapYear(year) ? 29 : 28;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else {
            return 31;
        }
    }

    // Helper method to check if a year is a leap year
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public void writeScheduleToFile(File filename, int startDate, int endDate) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Task> tasksWithinDates = getTasksWithinDates(startDate, endDate);

        // Exclude anti-tasks and add occurrences of recurring tasks
        ArrayList<Task> tasksToWriteToFile = new ArrayList<>();
        for (Task task : tasksWithinDates) {
            // Exclude anti-tasks
            if (task instanceof AntiTask) {
                continue;
            }
            // For recurring tasks, add each occurrence
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;
                for (int date : recurringTask.getAllOccurrenceDates()) {
                    // Check if there is an anti-task for this occurrence
                    boolean hasAntiTask = false;
                    for (Task antiTask : listOfTasks) {
                        if (antiTask instanceof AntiTask && antiTask.Date == date) {
                            hasAntiTask = true;
                            break;
                        }
                    }
                    // If there's no anti-task, add the occurrence to the list
                    if (!hasAntiTask) {
                        Task occurrenceTask = new Task(task.Name, task.Type, date, task.StartTime, task.Duration);
                        tasksToWriteToFile.add(occurrenceTask);
                    }
                }
            } else {
                tasksToWriteToFile.add(task); // For non-recurring tasks, add directly
            }
        }
        
        sortTasksByDateAndTime(tasksToWriteToFile);
        
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(tasksToWriteToFile, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to filter tasks within specified dates
    private static List<Task> getTasksWithinDates(int startDate, int endDate) {
        List<Task> tasksWithinDates = new ArrayList<>();
        for (Task task : listOfTasks) {
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;
                // create a modified recurring task with the given date range
                Task occurrenceTask = new RecurringTask(recurringTask.Name, recurringTask.Type, startDate, recurringTask.StartTime, recurringTask.Duration, endDate, recurringTask.Frequency);
                tasksWithinDates.add(occurrenceTask);
            } else if (task.Date >= startDate && task.Date <= endDate) {
                tasksWithinDates.add(task);
            }
        }
        sortTasksByDateAndTime(tasksWithinDates);
        return tasksWithinDates;
    }
    
    //methods to sort a list by the date and time
    public static void sortTasksByDateAndTime(List<Task> listOfTasks) {
        Collections.sort(listOfTasks, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                // Compare dates
                int dateComparison = Integer.compare(task1.Date, task2.Date);
                if (dateComparison != 0) {
                    return dateComparison;
                }
                // If dates are the same, compare start times
                return Float.compare(task1.StartTime, task2.StartTime);
            }
        });
    }

}
