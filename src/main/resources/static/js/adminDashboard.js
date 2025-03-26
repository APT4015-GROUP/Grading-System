// Fetch and display upcoming assignments
function fetchAllAssignments() {
    fetch("/api/assignments/all")
        .then((response) => response.json())
        .then((data) => {
            const assignmentsContainer = document.querySelector(".assignment-list");
            if (!assignmentsContainer) {
                console.error("Error: Assignment container not found.");
                return;
            }

            assignmentsContainer.innerHTML = ""; // Clear existing content 

            // Loop through the assignments and create a list item for each one
            data.forEach((assignment) => {
                const li = document.createElement("li");
                li.classList.add("assignment-item");

                const dueDate = new Date(assignment.dueDate?.toString() || "");

                li.innerHTML = `
                    <div class="details">
                        <div class="assignment-title">${assignment.title}</div>
                        <div class="assignment-description">Description: ${assignment.description}</div>
                        <div class="due-date">Due: ${dueDate.toLocaleDateString()}</div>
                    </div>
                    <button class="btn-primary" onclick="goToSubmission('${assignment.id}', '${assignment.title}', '${assignment.description}', '${assignment.dueDate}')">
                        View Submissions
                    </button>
                `;

                assignmentsContainer.appendChild(li);
            });
        })
        .catch((error) => {
            console.error("Error fetching assignments:", error);
        });
}

fetchAllAssignments();

// Redirect to the submitted assignments page
function goToSubmission(id, title, description, dueDate) {
    const assignmentData = { id, title, description, dueDate };

    fetch("/api/assignments/pushAssignmentDetails", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(assignmentData)
    })
        .then((response) => {
            if (response.ok) {
                window.location.href = "/submissions-page";
            } else {
                throw new Error("Failed to push assignment details.");
            }
        })
        .catch((error) => {
            console.error("Error:", error);
            alert("An error occurred while saving assignment details.");
        });
}
