import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Route, BrowserRouter as Router, Routes } from "react-router-dom"; 
import "./index.css";
import RegisterPage from "./pages/userAuth/Register";
import Login from "./pages/userAuth/Login";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Router>
    <Routes>
        <Route path="/login" element={<Login/>} />
        <Route path="/register" element={<RegisterPage/>} />
      </Routes>
    </Router>
  </StrictMode>
);
