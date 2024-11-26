package com.example.application.views.lecturer;

import com.example.application.data.*;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.AssignmentService;
import com.example.application.services.DocumentProcessingService;
import com.example.application.services.SubmissionService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "lecturer/grade-submissions", layout = MainLayout.class)
@PageTitle("Grade Submissions | SAGE")
@RolesAllowed("ROLE_LECTURER")
public class GradeSubmissionsView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(GradeSubmissionsView.class);
    
    private final Grid<Submission> submissionGrid = new Grid<>(Submission.class, false);
    private final Select<Assignment> assignmentSelect = new Select<>();
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final AuthenticatedUser authenticatedUser;
    private final DocumentProcessingService documentProcessingService;

    public GradeSubmissionsView(AssignmentService assignmentService,
                              SubmissionService submissionService,
                              AuthenticatedUser authenticatedUser,
                              DocumentProcessingService documentProcessingService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.authenticatedUser = authenticatedUser;
        this.documentProcessingService = documentProcessingService;

        setupLayout();
        setupAssignmentSelect();
        setupSubmissionGrid();
    }

    private void setupLayout() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("1200px");
        add(new H2("Grade Submissions"));
    }

    private void setupAssignmentSelect() {
        User lecturer = authenticatedUser.get().orElseThrow();
        List<Assignment> assignments = assignmentService.getLecturerAssignments(lecturer);
        
        assignmentSelect.setLabel("Select Assignment");
        assignmentSelect.setItems(assignments);
        assignmentSelect.setItemLabelGenerator(Assignment::getTitle);
        assignmentSelect.addValueChangeListener(event -> loadSubmissions(event.getValue()));
        
        add(assignmentSelect);
    }

    private void setupSubmissionGrid() {
        submissionGrid.addColumn(submission -> 
            submission.getStudent().getUser().getFirstName() + " " + 
            submission.getStudent().getUser().getLastName())
            .setHeader("Student Name");
            
        submissionGrid.addColumn(submission -> 
            submission.getStudent().getStudentNumber())
            .setHeader("Student Number");
            
        submissionGrid.addColumn(Submission::getFileName)
            .setHeader("File Name");
            
        submissionGrid.addColumn(submission -> 
            submission.getSubmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .setHeader("Submission Date");
            
        submissionGrid.addComponentColumn(submission -> {
            Button viewButton = new Button("View Submission", e -> {
                try {
                    viewSubmission(submission);
                } catch (Exception ex) {
                    logger.error("Error viewing submission: ", ex);
                    Notification.show("Error viewing submission: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            return viewButton;
        });

        add(submissionGrid);
    }

    @Transactional
    private void loadSubmissions(Assignment assignment) {
        if (assignment != null) {
            try {
                List<Submission> submissions = submissionService.getAssignmentSubmissions(assignment);
                logger.info("Loaded {} submissions for assignment {}", 
                          submissions.size(), assignment.getTitle());
                submissionGrid.setItems(submissions);
            } catch (Exception e) {
                logger.error("Error loading submissions: ", e);
                Notification.show("Error loading submissions: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

   @Transactional(readOnly = true)
    private void viewSubmission(Submission submission) {
        try {
            // Use the enhanced repository method to fetch the submission with all its associations
            Submission refreshedSubmission = submissionService.getSubmissionById(submission.getId());
            
            if (refreshedSubmission.getFileData() == null || refreshedSubmission.getFileData().length == 0) {
                logger.error("File data is empty for submission ID: {}", submission.getId());
                Notification.show("Error: The submission file appears to be empty or corrupted")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            logger.info("Opening submission dialog for submission ID: {}, file size: {} bytes", 
                       submission.getId(), refreshedSubmission.getFileData().length);
            
            SubmissionDialog dialog = new SubmissionDialog(refreshedSubmission, documentProcessingService);
            dialog.open();
        } catch (Exception e) {
            logger.error("Error viewing submission: ", e);
            Notification.show("Error viewing submission: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}