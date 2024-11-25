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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Route(value = "lecturer/grade-submissions", layout = MainLayout.class)
@PageTitle("Grade Submissions | SAGE")
@RolesAllowed("ROLE_LECTURER")
public class GradeSubmissionsView extends VerticalLayout {
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
           Button viewButton = new Button("View Submission", e -> viewSubmission(submission));
           return viewButton;
       });

       add(submissionGrid);
   }

   @Transactional(readOnly = true)
    private void loadSubmissions(Assignment assignment) {
        if (assignment != null) {
            List<Submission> submissions = submissionService.getAssignmentSubmissions(assignment);
            submissionGrid.setItems(submissions);
        }
}

   private void viewSubmission(Submission submission) {
       SubmissionDialog dialog = new SubmissionDialog(submission, documentProcessingService);
       dialog.open();
   }
}