function copyToClipboard(div) {
    // Select the text inside the <code> tag
    const codeElement = document.getElementById(div);

    // Create a temporary textarea to copy text
    const textarea = document.createElement("textarea");
    textarea.value = codeElement.innerText;  // Get text content

    // Append to body, copy, and remove
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);

    // Show a success message
    alert("Copied to clipboard!");
}

async function contact(event) {
    event.preventDefault(); // Prevent the default form submission

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const message = document.getElementById("message").value.trim();

    // Validate that all fields are filled
    if (!name || !email || !phone || !message) {
        alert("All fields are required!");
        return;
    }

    const body = { name:name, email:email, phone:phone, message:message };

    try {
        const response = await fetch("https://kundali.mbstudioz.in/contact", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        if (response.ok) {
            alert("Message Sent! Please wait while we contact you.");
            document.getElementById("contact-form").reset(); // Clear form after submission
        } else {
            alert("Error occurred! Email us at mainakbanerjee100@gmail.com");
        }
    } catch (error) {
        alert("Network error: " + error.message);
    }
}

// Attach event listener to the form
document.getElementById("contact-form").addEventListener("submit", contact);


