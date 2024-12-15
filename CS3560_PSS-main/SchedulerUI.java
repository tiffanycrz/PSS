import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;

public class SchedulerUI extends JFrame implements ActionListener {
    public static PSS PSSobject;
    private JPanel buttonPanel;

    //Sets up the UI
    public SchedulerUI() {
        
    	//initialize PSS
        PSSobject = new PSS();
        
//temp tasks for testing
        PSSobject.listOfTasks.add(new TransientTask("Having Fun", "Visit", 20240510, 4.5f, 1f));
        PSSobject.listOfTasks.add(new RecurringTask("CS Class", "Class", 20240515, 4.5f, 1f, 20240530, 1));
        PSSobject.listOfTasks.add(new TransientTask("Yippiddeedoo", "Visit", 20240518, 12f, 1f));
        PSSobject.listOfTasks.add(new AntiTask("Cancel Class", "Cancellation", 20240525, 4.5f, 1f));
//end of temp tasks
        
        setTitle("Scheduler");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buttonPanel = new JPanel(new GridLayout(4, 2));

        String[] buttonLabels = {
                "Create Task",
                "Search/Edit/Delete Task By Name",
                "View All Tasks",
                "Load Schedule",
                "View Schedule",
                "Save Schedule",
                "Exit"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
    }
    
    //Call to get back to original scene
    private void resetToMainUI() {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(4, 2));

        String[] buttonLabels = {
        		"Create Task",
                "Search/Edit/Delete Task By Name",
                "View All Tasks",
                "Load Schedule",
                "View Schedule",
                "Save Schedule",
                "Exit"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        revalidate();
        repaint();
    }

 // Call to get the UI for the create task scene
    private void createTaskUI() {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(0, 1));

        // Name selection
        JPanel namePanel = returnNameJPanel();
        buttonPanel.add(namePanel);
        buttonPanel.add(new JPanel());

        // Type selection (Text field instead of radio buttons)
        JLabel typeLabel = new JLabel("Type (Class, Study, Sleep, Exercise, Work, Meal, Visit, Shopping, Appointment, Cancellation):");
        JTextField typeField = new JTextField(20); // Adjust the size as needed

        buttonPanel.add(typeLabel);
        buttonPanel.add(typeField);
        buttonPanel.add(new JPanel());

        // Date selection
        JComboBox<Integer>[] dateComboBoxes = new JComboBox[3];
        JPanel datePanel = returnDateJPanel(dateComboBoxes);
        buttonPanel.add(datePanel);
        buttonPanel.add(new JPanel());

        // Start Time selection
        JComboBox<Integer>[] startTimeComboBoxes = new JComboBox[2];
        JPanel startTimePanel = returnStartTimeJPanel(startTimeComboBoxes, "Start Time");
        buttonPanel.add(startTimePanel);
        buttonPanel.add(new JPanel());

        // Duration Selection
        JPanel durationPanel = returnDurationJPanel();
        buttonPanel.add(durationPanel);
        buttonPanel.add(new JPanel());

        // Additional parameters for recurring tasks
        
        JLabel endDateLabel = new JLabel("Enter End Date (YYYYMMDD):");
        JTextField endDateField = new JTextField();
        JLabel frequencyLabel = new JLabel("Enter frequency (1 = everyday, 7 = weekly):");
        JTextField frequencyField = new JTextField();
        buttonPanel.add(endDateLabel);
        buttonPanel.add(endDateField);
        buttonPanel.add(frequencyLabel);
        buttonPanel.add(frequencyField);
        endDateLabel.setVisible(false);
        endDateField.setVisible(false);
        frequencyLabel.setVisible(false);
        frequencyField.setVisible(false);
        // Action listener for showing additional parameters for recurring tasks
        typeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String enteredType = typeField.getText().trim().toLowerCase();
                if (enteredType.equals("class") || enteredType.equals("study") || enteredType.equals("sleep") ||
                    enteredType.equals("exercise") || enteredType.equals("work") || enteredType.equals("meal")) {
                    endDateLabel.setVisible(true);
                    endDateField.setVisible(true);
                    frequencyLabel.setVisible(true);
                    frequencyField.setVisible(true);
                } else {
                	endDateLabel.setVisible(false);
                    endDateField.setVisible(false);
                    frequencyLabel.setVisible(false);
                    frequencyField.setVisible(false);
                }
                revalidate();
                repaint();
            }
        });

        // Save and Cancel buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JPanel saveCancelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveCancelPanel.add(saveButton);
        saveCancelPanel.add(cancelButton);
        buttonPanel.add(saveCancelPanel);

        saveButton.addActionListener(e -> {
            // Obtain all entries
            String name = getSelectionFromPanel(namePanel);
            String type = typeField.getText().trim();

            int selectedYear = (int) getSelectionFromPanel(datePanel, dateComboBoxes[0]);
            int selectedMonth = (int) getSelectionFromPanel(datePanel, dateComboBoxes[1]);
            int selectedDay = (int) getSelectionFromPanel(datePanel, dateComboBoxes[2]);
            int date = selectedYear * 10000 + selectedMonth * 100 + selectedDay;

            int startHours = (int) getSelectionFromPanel(startTimePanel, startTimeComboBoxes[0]);
            int startMinutes = (int) getSelectionFromPanel(startTimePanel, startTimeComboBoxes[1]);
            float startTime = convertTimeToFloat(startHours, startMinutes);

            String durationString = getSelectionFromPanel(durationPanel);
            float duration = convertStringTimeToFloat(durationString);

            if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("study") || type.equalsIgnoreCase("sleep") ||
                    type.equalsIgnoreCase("exercise") || type.equalsIgnoreCase("work") || type.equalsIgnoreCase("meal")) {
                String endDateText = endDateField.getText().trim();
                int endDate = Integer.parseInt(endDateText);

                String frequencyText = frequencyField.getText().trim();
                int frequency = Integer.parseInt(frequencyText);
                PSSobject.createTask(name, type, date, startTime, duration, endDate, frequency);
                
            } else {
                if(PSSobject.checkforDateTimeOverlap(date, startTime, duration) && !PSSobject.hasAntiTaskForDateTime(date, startTime)) {  
                    JOptionPane.showMessageDialog(null, "Date/Time Overlap Detected. Task not saved", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {  
                    PSSobject.createTask(name, type, date, startTime, duration);
                }
            }
            resetToMainUI();
        });

        cancelButton.addActionListener(e -> {
            resetToMainUI();
        });

        revalidate();
        repaint();
    }

	
    
    //Call to get the UI to edit a task. Pass the Task as a parameter
    private void editTasksUI(Task task) {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(0, 1));
        
        // Name selection
        JPanel namePanel = returnNameJPanel(task.Name);
        buttonPanel.add(namePanel);
        buttonPanel.add(new JPanel());
        
        // Type selection (Text field instead of radio buttons)
        JLabel typeLabel = new JLabel("Type (Class, Study, Sleep, Exercise, Work, Meal, Visit, Shopping, Appointment, Cancellation):");
        JTextField typeField = new JTextField(task.Type, 20); // Adjust the size as needed

        buttonPanel.add(typeLabel);
        buttonPanel.add(typeField);
        buttonPanel.add(new JPanel());

        // Date selection
        JComboBox<Integer>[] dateComboBoxes = new JComboBox[3];
        JPanel datePanel = returnDateJPanel(task.Date, dateComboBoxes);
        buttonPanel.add(datePanel);
        buttonPanel.add(new JPanel());

        // Start Time selection
        JComboBox<Integer>[] startTimeComboBoxes = new JComboBox[2];
        JPanel startTimePanel = returnStartTimeJPanel(task.StartTime, startTimeComboBoxes, "Start Time");
        buttonPanel.add(startTimePanel);
        buttonPanel.add(new JPanel());

        // Duration Selection
        JPanel durationPanel = returnDurationJPanel(task.Duration);
        buttonPanel.add(durationPanel);
        buttonPanel.add(new JPanel());

        // Additional parameters for recurring tasks
        JLabel endDateLabel = new JLabel("Enter End Date (YYYYMMDD):");
        JTextField endDateField = new JTextField();
        JLabel frequencyLabel = new JLabel("Enter frequency (1 = everyday, 7 = weekly):");
        JTextField frequencyField = new JTextField();
        buttonPanel.add(endDateLabel);
        buttonPanel.add(endDateField);
        buttonPanel.add(frequencyLabel);
        buttonPanel.add(frequencyField);
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            endDateField.setText(String.valueOf(recurringTask.EndDate));
            frequencyField.setText(String.valueOf(recurringTask.Frequency));
            endDateLabel.setVisible(true);
            endDateField.setVisible(true);
            frequencyLabel.setVisible(true);
            frequencyField.setVisible(true);
        } else {
            endDateLabel.setVisible(false);
            endDateField.setVisible(false);
            frequencyLabel.setVisible(false);
            frequencyField.setVisible(false);
        }
        
        // Action listeners for showing additional parameters for recurring tasks
        typeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String enteredType = typeField.getText().trim().toLowerCase();
                if (enteredType.equals("class") || enteredType.equals("study") || enteredType.equals("sleep") ||
                    enteredType.equals("exercise") || enteredType.equals("work") || enteredType.equals("meal")) {
                    endDateLabel.setVisible(true);
                    endDateField.setVisible(true);
                    frequencyLabel.setVisible(true);
                    frequencyField.setVisible(true);
                } else {
                    endDateLabel.setVisible(false);
                    endDateField.setVisible(false);
                    frequencyLabel.setVisible(false);
                    frequencyField.setVisible(false);
                }
                revalidate();
                repaint();
            }
        });

        // Save and Cancel buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JPanel saveCancelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveCancelPanel.add(saveButton);
        saveCancelPanel.add(cancelButton);
        buttonPanel.add(saveCancelPanel);

        saveButton.addActionListener(e -> {
            // Obtain all entries
            String name = getSelectionFromPanel(namePanel);
            String type = typeField.getText().trim();

            int selectedYear = (int) getSelectionFromPanel(datePanel, dateComboBoxes[0]);
            int selectedMonth = (int) getSelectionFromPanel(datePanel, dateComboBoxes[1]);
            int selectedDay = (int) getSelectionFromPanel(datePanel, dateComboBoxes[2]);
            int date = selectedYear * 10000 + selectedMonth * 100 + selectedDay;

            int startHours = (int) getSelectionFromPanel(startTimePanel, startTimeComboBoxes[0]);
            int startMinutes = (int) getSelectionFromPanel(startTimePanel, startTimeComboBoxes[1]);
            float startTime = convertTimeToFloat(startHours, startMinutes);

            String durationString = getSelectionFromPanel(durationPanel);
            float duration = convertStringTimeToFloat(durationString);

            if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("study") || type.equalsIgnoreCase("sleep") ||
                type.equalsIgnoreCase("exercise") || type.equalsIgnoreCase("work") || type.equalsIgnoreCase("meal")) {
                String endDateText = endDateField.getText().trim();
                int endDate = Integer.parseInt(endDateText);

                String frequencyText = frequencyField.getText().trim();
                int frequency = Integer.parseInt(frequencyText);
                PSSobject.editTask(task, name, type, date, startTime, duration, endDate, frequency);
                
            } else {
                PSSobject.editTask(task, name, type, date, startTime, duration);
            }
            resetToMainUI();
        });

        cancelButton.addActionListener(e -> {
            resetToMainUI();
        });

        revalidate();
        repaint();
    }


    
    private void viewTaskUI(Task task) {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(0, 1));
        // Name panel
        JLabel nameLabel = new JLabel("Name:");
        JLabel nameValueLabel = new JLabel(task.Name);
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(nameLabel);
        namePanel.add(nameValueLabel);
        buttonPanel.add(namePanel);
        buttonPanel.add(new JPanel());

        // Type panel
        JLabel typeLabel = new JLabel("Type:");
        JLabel typeValueLabel = new JLabel(task.Type);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(typeLabel);
        typePanel.add(typeValueLabel);
        buttonPanel.add(typePanel);
        buttonPanel.add(new JPanel());

        // Date panel
        JLabel dateLabel = new JLabel("Date:");
        JLabel dateValueLabel = new JLabel(Integer.toString(task.Date));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(dateLabel);
        datePanel.add(dateValueLabel);
        buttonPanel.add(datePanel);
        buttonPanel.add(new JPanel());

        // Start Time panel
        JLabel startTimeLabel = new JLabel("Start Time:");
        JLabel startTimeValueLabel = new JLabel(Float.toString(task.StartTime));
        JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startTimePanel.add(startTimeLabel);
        startTimePanel.add(startTimeValueLabel);
        buttonPanel.add(startTimePanel);
        buttonPanel.add(new JPanel());

        // Duration panel
        JLabel durationLabel = new JLabel("Duration:");
        JLabel durationValueLabel = new JLabel(Float.toString(task.Duration));
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(durationLabel);
        durationPanel.add(durationValueLabel);
        buttonPanel.add(durationPanel);
        buttonPanel.add(new JPanel());

        // Additional characteristics for recurring tasks
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            JLabel endDateLabel = new JLabel("End Date:");
            JLabel endDateValueLabel = new JLabel(Integer.toString(recurringTask.EndDate));
            JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            endDatePanel.add(endDateLabel);
            endDatePanel.add(endDateValueLabel);
            buttonPanel.add(endDatePanel);
            buttonPanel.add(new JPanel());

            JLabel frequencyLabel = new JLabel("Frequency:");
            JLabel frequencyValueLabel = new JLabel(Integer.toString(recurringTask.Frequency));
            JPanel frequencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            frequencyPanel.add(frequencyLabel);
            frequencyPanel.add(frequencyValueLabel);
            buttonPanel.add(frequencyPanel);
            buttonPanel.add(new JPanel());
        }

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setToolTipText("Return to main menu");
        backButton.addActionListener(e -> {
            resetToMainUI();
        });
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backButton);
        buttonPanel.add(backPanel);

        revalidate();
        repaint();
    }
    
    private void deleteTaskUI(Task task) {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(0, 1));
        // Name panel
        JLabel nameLabel = new JLabel("Name:");
        JLabel nameValueLabel = new JLabel(task.Name);
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(nameLabel);
        namePanel.add(nameValueLabel);
        buttonPanel.add(namePanel);
        buttonPanel.add(new JPanel());

        // Type panel
        JLabel typeLabel = new JLabel("Type:");
        JLabel typeValueLabel = new JLabel(task.Type);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(typeLabel);
        typePanel.add(typeValueLabel);
        buttonPanel.add(typePanel);
        buttonPanel.add(new JPanel());

        // Date panel
        JLabel dateLabel = new JLabel("Date:");
        JLabel dateValueLabel = new JLabel(Integer.toString(task.Date));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(dateLabel);
        datePanel.add(dateValueLabel);
        buttonPanel.add(datePanel);
        buttonPanel.add(new JPanel());

        // Start Time panel
        JLabel startTimeLabel = new JLabel("Start Time:");
        JLabel startTimeValueLabel = new JLabel(Float.toString(task.StartTime));
        JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startTimePanel.add(startTimeLabel);
        startTimePanel.add(startTimeValueLabel);
        buttonPanel.add(startTimePanel);
        buttonPanel.add(new JPanel());

        // Duration panel
        JLabel durationLabel = new JLabel("Duration:");
        JLabel durationValueLabel = new JLabel(Float.toString(task.Duration));
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(durationLabel);
        durationPanel.add(durationValueLabel);
        buttonPanel.add(durationPanel);
        buttonPanel.add(new JPanel());

        // Additional characteristics for recurring tasks
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            JLabel endDateLabel = new JLabel("End Date:");
            JLabel endDateValueLabel = new JLabel(Integer.toString(recurringTask.EndDate));
            JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            endDatePanel.add(endDateLabel);
            endDatePanel.add(endDateValueLabel);
            buttonPanel.add(endDatePanel);
            buttonPanel.add(new JPanel());

            JLabel frequencyLabel = new JLabel("Frequency:");
            JLabel frequencyValueLabel = new JLabel(Integer.toString(recurringTask.Frequency));
            JPanel frequencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            frequencyPanel.add(frequencyLabel);
            frequencyPanel.add(frequencyValueLabel);
            buttonPanel.add(frequencyPanel);
            buttonPanel.add(new JPanel());
        }

        // Delete and Cancel buttons
        JButton deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Delete task");
        deleteButton.addActionListener(e -> {
            PSSobject.deleteTask(task);
            resetToMainUI();
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel deletion");
        cancelButton.addActionListener(e -> {
            resetToMainUI();
        });
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(cancelButton);
        buttonPanel.add(buttonsPanel);

        revalidate();
        repaint();
    }


    private void displayTasksUI(Task task) {
    	ArrayList<Task> taskList = new ArrayList<Task>();
    	taskList.add(task);
    	displayTasksUI(taskList);
    }
    //Call to get a UI list of all Tasks. Can Choose to edit/delete each one
    private void displayTasksUI(ArrayList<Task> taskList) {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new BorderLayout());
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        
        JScrollPane tasksScrollPane = new JScrollPane(tasksPanel);
        tasksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Iterate over each task in the listOfTasks
        for (Task task : taskList) {
            // Create a panel to display the task attributes
            JPanel taskPanel = new JPanel(new BorderLayout());
            taskPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility
            JLabel taskInfoLabel;
            
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;
            	// Create a label to display task information
                taskInfoLabel = new JLabel(
                        "<html><b>Name:</b> " + recurringTask.Name +
                                "<br><b>Type:</b> " + recurringTask.Type +
                                "<br><b>Start Date:</b> " + recurringTask.StartDate +
                                "<br><b>Start Time:</b> " + recurringTask.StartTime +
                                "<br><b>Duration:</b> " + recurringTask.Duration +
                                "<br><b>End Date:</b> " + recurringTask.EndDate +
                                "<br><b>Frequency:</b> " + recurringTask.Frequency + "<html>"
                );
            }
            else {
            	// Create a label to display task information
                taskInfoLabel = new JLabel(
                        "<html><b>Name:</b> " + task.Name +
                                "<br><b>Type:</b> " + task.Type +
                                "<br><b>Date:</b> " + task.Date +
                                "<br><b>Start Time:</b> " + task.StartTime +
                                "<br><b>Duration:</b> " + task.Duration + "</html>"
                );
            }

            taskInfoLabel.setOpaque(true); // Enable background color
            taskInfoLabel.setBackground(Color.WHITE); // Set default background color

            taskPanel.add(taskInfoLabel, BorderLayout.CENTER);

            // Create a dropdown menu for edit and delete options
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem viewMenuItem = new JMenuItem("View Task");
            JMenuItem editMenuItem = new JMenuItem("Edit Task");
            JMenuItem deleteMenuItem = new JMenuItem("Delete Task");

            // Add action listeners for edit and delete options (implement later)
            viewMenuItem.addActionListener(e -> {
                viewTaskUI(task);
            });
            editMenuItem.addActionListener(e -> {
                editTasksUI(task);
            });
            deleteMenuItem.addActionListener(e -> {
                deleteTaskUI(task);
            });

            popupMenu.add(viewMenuItem);
            popupMenu.add(editMenuItem);
            popupMenu.add(deleteMenuItem);

            // Add mouse listener to show dropdown menu on right-click
            taskPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        popupMenu.show(taskPanel, e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    taskInfoLabel.setBackground(Color.LIGHT_GRAY); // Change background color on hover
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    taskInfoLabel.setBackground(Color.WHITE); // Restore default background color on exit
                }
            });

            // Add the task panel to the tasks panel
            tasksPanel.add(taskPanel);
        }

        // Add the tasks scroll pane to the center of the button panel
        buttonPanel.add(tasksScrollPane, BorderLayout.CENTER);

        // Create a back button
        JButton backButton = new JButton("Back");
        backButton.setToolTipText("Return to main menu");
        backButton.addActionListener(e -> {
            resetToMainUI();
        });

        // Add the back button to the bottom right corner of the button panel
        buttonPanel.add(backButton, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
    
    public void saveSchedule(JFrame frame) {
        // Clear buttonPanel
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(4, 3));

        // Create components for file selection and date input
        JLabel filePathLabel = new JLabel("File Path:");
        JTextField filePathField = new JTextField();
        JButton selectFileButton = new JButton("Select File");
        JLabel startDateLabel = new JLabel("Start Date:");
        JTextField startDateField = new JTextField();
        JLabel endDateLabel = new JLabel("End Date:");
        JTextField endDateField = new JTextField();
        JCheckBox saveAllCheckBox = new JCheckBox("Save All");

        // Add components to buttonPanel
        buttonPanel.add(filePathLabel);
        buttonPanel.add(filePathField);
        buttonPanel.add(selectFileButton);
        buttonPanel.add(startDateLabel);
        buttonPanel.add(startDateField);
        buttonPanel.add(new JLabel()); // Placeholder
        buttonPanel.add(endDateLabel);
        buttonPanel.add(endDateField);
        buttonPanel.add(saveAllCheckBox);

        // Action listener for "Select File" button
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // Item listener for "Save All" checkbox
        saveAllCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (saveAllCheckBox.isSelected()) {
                    startDateField.setText("10000101");
                    endDateField.setText("99991231");
                } else {
                    startDateField.setText("");
                    endDateField.setText("");
                }
            }
        });

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected file path
                String filePath = filePathField.getText();
                File selectedFile = new File(filePath);

                // Get start and end dates from text fields
                String startDateText = startDateField.getText().trim();
                String endDateText = endDateField.getText().trim();

                // Input Validation
                if(!isAllNumbers(startDateText) || !isAllNumbers(endDateText)) {
                    JOptionPane.showMessageDialog(null, "ERROR: Please enter only numbers in the form YYYYMMDD.");    
                }
                else {
                    // Extract the actual dates from the text fields
                    int startDate = Integer.parseInt(startDateText);
                    int endDate = Integer.parseInt(endDateText);
    
                    // Call createScheduleToFile method with selected file and dates
                    PSSobject.writeScheduleToFile(selectedFile, startDate, endDate);
    
                    // Inform user about successful save
                    JOptionPane.showMessageDialog(null, "Schedule saved successfully!");
    
                    // Reset to main UI
                    resetToMainUI();
                }
            }
        });
        buttonPanel.add(saveButton);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToMainUI();
            }
        });
        buttonPanel.add(cancelButton);

        revalidate();
        repaint();
    }

	private boolean isAllNumbers(String input)
    	{
        	for (int i = 0; i < input.length(); i++) {
            	if(!Character.isDigit(input.charAt(i)))
                	return false;
        	}
        	return true;
    	}


    private void loadScheduleUI() {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(4, 2));

        JLabel filePathLabel = new JLabel("File Path:");
        JTextField filePathField = new JTextField();
        JButton chooseFileButton = new JButton("Choose File");
        JRadioButton addToCurrentRadio = new JRadioButton("Add to current schedule", true);
        JRadioButton createNewRadio = new JRadioButton("Create new schedule");
        JButton loadButton = new JButton("Load");
        JButton cancelButton = new JButton("Cancel");

        ButtonGroup group = new ButtonGroup();
        group.add(addToCurrentRadio);
        group.add(createNewRadio);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();
                File file = new File(filePath);
                boolean addToCurrent = addToCurrentRadio.isSelected();
                PSSobject.readScheduleFromFile(file, addToCurrent);
                resetToMainUI();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToMainUI();
            }
        });

        buttonPanel.add(filePathLabel);
        buttonPanel.add(filePathField);
        buttonPanel.add(new JLabel()); // Placeholder
        buttonPanel.add(chooseFileButton);
        buttonPanel.add(addToCurrentRadio);
        buttonPanel.add(createNewRadio);
        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);

        revalidate();
        repaint();
    }
//View the schedule of a specific day, week, or month
    private void viewScheduleUI() {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(4, 2));

        // Clear button panel
        buttonPanel.removeAll();

        // Add start date entry
        JLabel startDateLabel = new JLabel("Start Date (YYYYMMDD):");
        JTextField startDateField = new JTextField();
        startDateField.setPreferredSize(new Dimension(200, startDateField.getPreferredSize().height));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(startDateLabel);
        datePanel.add(startDateField);
        
        buttonPanel.add(datePanel);

        // Add radio buttons for view mode selection
        JRadioButton dayRadioButton = new JRadioButton("Day");
        JRadioButton weekRadioButton = new JRadioButton("Week");
        JRadioButton monthRadioButton = new JRadioButton("Month");
        ButtonGroup viewModeGroup = new ButtonGroup();
        viewModeGroup.add(dayRadioButton);
        viewModeGroup.add(weekRadioButton);
        viewModeGroup.add(monthRadioButton);

        JPanel radioPanel = new JPanel(new GridLayout(1, 3));
        radioPanel.add(dayRadioButton);
        radioPanel.add(weekRadioButton);
        radioPanel.add(monthRadioButton);
        buttonPanel.add(radioPanel);

        // Add view button
        JButton viewButton = new JButton("View");
        buttonPanel.add(viewButton);

        // Add back button
        JButton backButton = new JButton("Back");
        buttonPanel.add(backButton);

        // Action listener for view button
        viewButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get start date from the text field
                String startDateText = startDateField.getText().trim();
             // Parses through the string to detect any non-numerical characters
                if(!isAllNumbers(startDateText)) {
                    JOptionPane.showMessageDialog(null, "ERROR: Please enter only numbers in the form YYYYMMDD.");    
                }
                else {
                    // Remove the placeholder text if it exists
                    //startDateText = startDateText.replace("Enter Start Date (YYYYMMDD):", "");
                    int startDate = Integer.parseInt(startDateText);

                    // Determine the selected view mode
                    String viewMode = "";
                    if (dayRadioButton.isSelected()) {
                        viewMode = "Day";
                    } else if (weekRadioButton.isSelected()) {
                        viewMode = "Week";
                    } else if (monthRadioButton.isSelected()) {
                        viewMode = "Month";
                    }

                    // Call the viewSchedule method with the selected parameters
                    ArrayList<Task> schedule = PSSobject.viewSchedule(startDate, viewMode);
                    displayTasksUI(schedule);
                }
            }
        });

        // Action listener for back button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToMainUI();
            }
        });

        revalidate();
        repaint();
    }

    private void searchTaskUI() {
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(0, 1));

        JLabel searchLabel = new JLabel("Search by name:");
        buttonPanel.add(searchLabel);

        JTextField searchField = new JTextField(20);
        buttonPanel.add(searchField);

        buttonPanel.add(new JPanel());

        JButton searchButton = new JButton("Search");
        JButton cancelButton = new JButton("Cancel");

        JPanel searchCancelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchCancelPanel.add(searchButton);
        searchCancelPanel.add(cancelButton);

        buttonPanel.add(searchCancelPanel);

        // Add action listeners for search and cancel buttons
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                Task foundTask = PSSobject.getTaskByName(searchTerm);
                displayTasksUI(foundTask);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Please enter a task name to search.", "Empty Search Term", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            resetToMainUI();
        });

        // Repaint the panel
        revalidate();
        repaint();
    }
    
//Overloaded name functions. Returns a JPanel for the UI
    public JPanel returnNameJPanel() {
        return returnNameJPanel("");
    }
    public JPanel returnNameJPanel(String name) {
    	JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(name);
        nameField.setPreferredSize(new Dimension(200, nameField.getPreferredSize().height));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(nameLabel);
        namePanel.add(nameField);
        return namePanel;
    }
    
//Overloaded Date functions. Returns a JPanel for the UI
    public JPanel returnDateJPanel(JComboBox<Integer>[] comboBoxes) {
    	int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    	int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
    	int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	int currentFormattedDate = currentYear * 10000 + currentMonth * 100 + currentDay;
    	return returnDateJPanel(currentFormattedDate, comboBoxes); 	
    }
    public JPanel returnDateJPanel(int date, JComboBox<Integer>[] comboBoxes) {
        // Extract year, month, and day from the provided date
        int year = date / 10000;
        int month = (date / 100) % 100;
        int day = date % 100;

        // Year
        JLabel yearLabel = new JLabel("Start Date - Year:");
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 50; y <= currentYear + 50; y++) {
            yearComboBox.addItem(y);
        }
        yearComboBox.setSelectedItem(year);

        // Month (Numbers from 1 to 12)
        JLabel monthLabel = new JLabel("Month:");
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int m = 1; m <= 12; m++) {
            monthComboBox.addItem(m);
        }
        monthComboBox.setSelectedItem(month);

        // Day selection
        JLabel dayLabel = new JLabel("Day:");
        JComboBox<Integer> dayComboBox = new JComboBox<>();
        updateDayComboBox(dayComboBox, month, year); // Update day combo box based on month and year
        dayComboBox.setSelectedItem(day);

        // Initially update dayComboBox based on the selected month and year
        int initialMonth = (int) monthComboBox.getSelectedItem();
        int initialYear = (int) yearComboBox.getSelectedItem();
        updateDayComboBox(dayComboBox, initialMonth, initialYear);
        JLabel startTimeLabel = new JLabel("Start Time:");

        // Add dates to nested panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(startTimeLabel);
        datePanel.add(yearLabel);
        datePanel.add(yearComboBox);
        datePanel.add(monthLabel);
        datePanel.add(monthComboBox);
        datePanel.add(dayLabel);
        datePanel.add(dayComboBox);
        
        comboBoxes[0] = yearComboBox;
        comboBoxes[1] = monthComboBox;
        comboBoxes[2] = dayComboBox;
        
        return datePanel;
    }
//Overloaded Start Time functions. Returns a JPanel for the UI   
    public JPanel returnStartTimeJPanel(JComboBox<Integer>[] comboBoxes, String title) {
    	
    	return returnStartTimeJPanel(0f, comboBoxes, title);
    }
	public JPanel returnStartTimeJPanel(float startTime, JComboBox<Integer>[] comboBoxes, String title) {
		// Combo box for start hours
        JComboBox<Integer> startTimeHourComboBox = new JComboBox<>();
        for (int i = 0; i < 24; i++) {
            startTimeHourComboBox.addItem(i);
        }
        // Combo box for start minutes
        JComboBox<Integer> startTimeMinuteComboBox = new JComboBox<>(new Integer[]{00, 15, 30, 45});

        JLabel startTimeLabel = new JLabel(title);
        JLabel startTimeColonLabel = new JLabel(":");
        
        // Create a nested panel for the time components
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(startTimeLabel);
        timePanel.add(startTimeHourComboBox);
        timePanel.add(startTimeColonLabel);
        timePanel.add(startTimeMinuteComboBox);
        
        comboBoxes[0] = startTimeHourComboBox;
        comboBoxes[1] = startTimeMinuteComboBox;
    	
        return timePanel;
    }
	
//Overloaded Duration functions. Returns a JPanel for the UI
	public JPanel returnDurationJPanel() {
    	
    	return returnDurationJPanel(0f);
    }
    
	public JPanel returnDurationJPanel(float time) {
		// Convert float time to hours and minutes
	    int[] timeArray = convertFloatToTime(time);
	    int hours = timeArray[0];
	    int minutes = timeArray[1];
		
		JLabel durationLineLabel = new JLabel("Duration:");
        JComboBox<String> durationComboBox = new JComboBox<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                durationComboBox.addItem(String.format("%02d:%02d", hour, minute));
            }
        }
        
        String selectedTime = String.format("%02d:%02d", hours, minutes);
        durationComboBox.setSelectedItem(selectedTime);
        
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(durationLineLabel);
        durationPanel.add(durationComboBox);
        
        return durationPanel;
    }

//Returns the selection of JTextField, JRadioButton, or JComboBox as a String
	public String getSelectionFromPanel(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                return textField.getText();
            }
            else if (component instanceof JRadioButton) {
                JRadioButton radioButton = (JRadioButton) component;
                if (radioButton.isSelected()) {
                    return radioButton.getText();
                }
            }  
            else if (component instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) component;
                return (String) comboBox.getSelectedItem();
            }
        }
        return null; 
    }
//Returns the selection of a specific JComboBox inside a specified JPanel as an int
    public int getSelectionFromPanel(JPanel panel, JComboBox<?> comboBox) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component.equals(comboBox)) {
                int selectedItem = (int) comboBox.getSelectedItem();
                return selectedItem;
            }
        }
        return 0;
    }
    
// Todo - doesn't work anymore for some reason? days are off for the months selected.
// Method to update the dayComboBox based on the selected month and year
    private void updateDayComboBox(JComboBox<Integer> dayComboBox, int selectedMonth, int selectedYear) {
        // Clear existing items in dayComboBox
        dayComboBox.removeAllItems();
        // Get the maximum number of days for the selected month and year
        int maxDay = PSSobject.getDaysInMonth(selectedMonth, selectedYear);
        // Populate dayComboBox with numbers from 1 to maxDay
        for (int day = 1; day <= maxDay; day++) {
            dayComboBox.addItem(day);
        }
    }
    
//Start of some conversion methods
    //converts hours and minutes into a float. ex hours = 13, minutes = 45 -> 13.75
    public float convertTimeToFloat(int hours, int minutes) {
        float minutesFraction = (float) minutes / 60;
        float totalHours = hours + minutesFraction;
        return totalHours;
    }
    //converts float into an array which the first index is hours and the second is minutes. ex. 13.75 -> int[13, 45]
    public int[] convertFloatToTime(float totalTime) {
        int hours = (int) totalTime;
        float minutesFloat = (totalTime - hours) * 60;
        int minutes = Math.round(minutesFloat);
        if (minutes == 60) {
            hours++;
            minutes = 0;
        }
        return new int[]{hours, minutes};
    }
    //converts the string in the format HH:MM to a float. ex. 13:45 -> 13.75
    public float convertStringTimeToFloat(String time) {
        // Split the time string into hours and minutes
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        
        float totalHours = convertTimeToFloat(hours, minutes);
        
        return totalHours;
    }

//If one of the buttons in the mainUI are selected this calls the function
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Create Task":
                createTaskUI(); 
                break;
            case "Search/Edit/Delete Task By Name":
            	searchTaskUI();; //display tasks to choose one to edit
                break;
            case "View All Tasks":
                displayTasksUI(PSSobject.getListOfTasks());;
                break;
            case "Load Schedule":
                loadScheduleUI();
                break;
            case "View Schedule":
                viewScheduleUI();
                break;
            case "Save Schedule":
                saveSchedule(null);
                break;
            case "Exit":
                System.exit(0);
                break;
        }
    }

//Sets the UI to be visible
    public static void startViewer() {
    	SchedulerUI schedulerUI = new SchedulerUI();
    	SwingUtilities.invokeLater(() -> {
            schedulerUI.setVisible(true);
    	});
    }
//main method
    public static void main(String[] args) {
    	startViewer();
    }
}
