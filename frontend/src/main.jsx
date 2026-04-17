import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";

// Initialize dark mode before render to prevent flash
const savedTheme = localStorage.getItem("theme");
const isDark = savedTheme ? savedTheme === "dark" : true; // Default dark
if (isDark) {
  document.documentElement.classList.add("ion-palette-dark");
}

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
