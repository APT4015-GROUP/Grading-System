package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    private String studentName;
    private String studentEmail;
    private Long assignmentId;
    private Long studentId;
}