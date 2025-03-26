document.getElementById("assignmentForm").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent the default form submission behavior

    const assignmentName = document.getElementById("assignmentName").value.trim();
    const dueDate = document.getElementById("dueDate").value; // No need to trim date
    const description = document.getElementById("description").value.trim();
    const uploadFile = document.getElementById("uploadFile")?.files[0];

    const submitButton = document.getElementById("submit_btn");
    const loading = document.getElementById("loader");

    // Check if required fields are filled
    if (!assignmentName || !dueDate || !description) {
        alert("Please fill in all required fields.");
        return;
    }

    // Disable the button to prevent further submissions
    submitButton.style.display = "none";
    loading.style.display = "block";

    // Create FormData to send file and form data together
    const formData = new FormData();
    formData.append("assignmentName", assignmentName);
    formData.append("dueDate", dueDate);
    formData.append("description", description);
    if (uploadFile) {
        formData.append("uploadFile", uploadFile);
    }

    // Send the data to the backend
    fetch("/api/assignments", {
        method: "POST",
        body: formData
    })
        .then((response) => {
            loading.style.display = "none";
            if (!response.ok) {
                throw new Error("Assignment creation failed");
            }
            return response.text();
        })
        .then((successMessage) => {
            loading.style.display = "none";
            submitButton.style.display = "inline";
            alert(successMessage);
            // Clear form after success
            document.getElementById("assignmentForm").reset();
            window.location.href = "/dashboard";
        })
        .catch((error) => {
            loading.style.display = "none";
            submitButton.style.display = "inline";
            alert("Error: " + error.message);
        });
});
