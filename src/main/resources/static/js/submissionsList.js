// Fetch submissions
function fetchSubmissions() {
    fetch("/submissions/existing")
        .then((response) => {
            if (!response.ok) {
                throw new Error("Failed to fetch submissions.");
            }
            return response.json();
        })
        .then((data) => {
            const assignmentsContainer = document.querySelector(".submission-list");
            assignmentsContainer.innerHTML = ""; // Clear existing content

            // Loop through the submissions and create a list item for each one
            data.forEach((submission) => {
                const li = document.createElement("li");
                li.classList.add("submission-item");

                li.innerHTML = `
                    <div class="details">
                        <div class="student-name">Name: ${submission.studentName}</div>
                        <div class="student-email">Email: ${submission.studentEmail}</div>            
                    </div>
                `;

                const button = document.createElement("button");
                button.classList.add("btn-checkGrade");
                button.textContent = "View Evaluation";
                
                button.addEventListener("click", () => {
                    goToEvaluation(submission.assignmentId, submission.studentId);
                });

                li.appendChild(button);
                assignmentsContainer.appendChild(li);
            });
        })
        .catch((error) => {
            console.error("Error fetching submissions:", error);
            alert("An error occurred while retrieving submissions.");
        });
}

// Redirect to the evaluations page
function goToEvaluation(assignmentId, studentId) {
    fetch(`/submissions/pushEvaluationDetails?assignmentId=${assignmentId}&studentId=${studentId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("Failed to save evaluation details.");
            }
            window.location.href = "/evaluation-page";
        })
        .catch((error) => {
            console.error("Error during evaluation redirection:", error);
            alert("An error occurred while saving evaluation details.");
        });
}

// Fetch submissions on page load
fetchSubmissions();
