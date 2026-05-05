// =====================
// LOGIN
// =====================
function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ username, password })
  })
  .then(res => res.json())
  .then(data => {
    if (data.token) {
      localStorage.setItem("token", data.token);
      alert("Login successful");
      window.location.href = "dashboard.html";
    } else {
      alert("Invalid username or password");
    }
  })
  .catch(err => {
    console.error(err);
    alert("Login error");
  });
}

// =====================
// REGISTER
// =====================
function register() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  fetch("http://localhost:8080/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ username, password })
  })
  .then(res => res.json())
  .then(() => {
    alert("Registration successful!");
    window.location.href = "login.html";
  })
  .catch(err => {
    console.error(err);
    alert("Registration failed");
  });
}

// =====================
// CREATE REQUEST
// =====================
function createRequest() {
  const token = localStorage.getItem("token");

  fetch("http://localhost:8080/api/requests", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token
    },
    body: JSON.stringify({
      email: document.getElementById("email").value,
      requestType: document.getElementById("type").value
    })
  })
  .then(res => {
    console.log("CREATE STATUS:", res.status);
    return res.text();
  })
  .then(() => {
    alert("Request submitted");
    loadRequests(); // refresh list
  })
  .catch(err => {
    console.error(err);
    alert("Error creating request");
  });
}

// =====================
// LOAD REQUESTS
// =====================
function loadRequests() {
  const token = localStorage.getItem("token");

  fetch("http://localhost:8080/api/requests", {
    headers: {
      "Authorization": "Bearer " + token
    }
  })
  .then(res => res.json())
  .then(data => {

    const list = document.getElementById("requestList");
    list.innerHTML = "";

    if (!data.content || data.content.length === 0) {
      list.innerHTML = "<li class='list-group-item'>No requests found</li>";
      return;
    }

    data.content.forEach(req => {

      let statusColor = "secondary";

      if (req.status === "APPROVED") statusColor = "success";
      if (req.status === "REJECTED") statusColor = "danger";
      if (req.status === "PENDING") statusColor = "warning";

      const li = document.createElement("li");
      li.className = "list-group-item";

      li.innerHTML = `
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <b>${req.email}</b><br>
            <small>${req.requestType}</small><br>
            <span class="badge bg-${statusColor}">${req.status}</span>
          </div>

          <div>
            <button class="btn btn-success btn-sm me-1"
              onclick="updateStatus(${req.id}, 'APPROVED')">✔</button>

            <button class="btn btn-danger btn-sm"
              onclick="updateStatus(${req.id}, 'REJECTED')">✖</button>
          </div>
        </div>
      `;

      list.appendChild(li);
    });
  })
  .catch(err => {
    console.error(err);
    alert("Error loading requests");
  });
}

// =====================
// UPDATE STATUS
// =====================
function updateStatus(id, status) {
  const token = localStorage.getItem("token");

  fetch(`http://localhost:8080/api/requests/updateStatus/${id}?status=${status}`, {
    method: "PUT",
    headers: {
      "Authorization": "Bearer " + token
    }
  })
  .then(res => res.json())
  .then(() => {
    alert("Status updated to " + status);
    loadRequests();
  })
  .catch(err => {
    console.error(err);
    alert("Error updating status");
  });
}

// =====================
// LOGOUT
// =====================
function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}