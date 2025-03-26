// Direct to dashboard
function backToDashboard() {
    window.location.href = "/dashboard";
}

// Handle file input trigger
function triggerFileInput() {
    document.getElementById("fileInput").click();
}

// Handle file upload
function handleFileUpload(event) {
    const fileInput = document.getElementById("fileInput");
    const fileList = document.getElementById("fileList");
    fileList.innerHTML = ""; // Clear the existing list

    const allowedExtensions = ["pdf", "doc", "docx"];

    Array.from(event.target.files).forEach((file) => {
        const fileExtension = file.name.split(".").pop().toLowerCase();

        if (!allowedExtensions.includes(fileExtension)) {
            alert(`File type not allowed: ${file.name}`);
            return;
        }

        const fileItem = document.createElement("div");
        fileItem.classList.add("file-item");

        const fileInfo = document.createElement("div");
        fileInfo.classList.add("file-info");
        fileInfo.innerHTML = `<span>📄</span><span>${file.name}</span><span>(${(file.size / (1024 * 1024)).toFixed(2)} MB)</span>`;

        const removeButton = document.createElement("button");
        removeButton.classList.add("btn", "btn-secondary");
        removeButton.textContent = "Remove";

        removeButton.addEventListener("click", () => {
            fileItem.remove(); // Remove from UI

            // Remove from file input list
            const dataTransfer = new DataTransfer();
            Array.from(fileInput.files).forEach((f) => {
                if (f !== file) {
                    dataTransfer.items.add(f);
                }
            });
            fileInput.files = dataTransfer.files;
        });

        fileItem.appendChild(fileInfo);
        fileItem.appendChild(removeButton);
        fileList.appendChild(fileItem);
    });
}

// Handle file submission
function submitFiles() {
    const submitButton = document.getElementById("submit_btn");
    const loading = document.getElementById("loader");

    const fileInput = document.getElementById("fileInput");
    const files = fileInput.files;

    if (files.length === 0) {
        alert("Please upload at least one file before submitting.");
        return;
    }

    // Disable the button to prevent further submissions
    submitButton.style.display = "none";
    loading.style.display = "block";

    const formData = new FormData();
    formData.append("file", files[0]);

    fetch("/submissions/evaluate", {
        method: "POST",
        body: formData
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((data) => {
                    throw new Error(data.error || "Failed to upload file.");
                });
            }
            return response.json();
        })
        .then((data) => {
            loading.style.display = "none";
            if (data.message) {
                alert(`Success: ${data.message}`);
                window.location.href = "/dashboard";
            } else {
                alert("Unknown server response.");
            }
        })
        .catch((error) => {
            loading.style.display = "none";
            submitButton.style.display = "inline";
            alert(`Error: ${error.message}`);
        });
}
