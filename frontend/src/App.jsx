import { Redirect, Route } from "react-router-dom";
import {
  IonApp,
  IonIcon,
  IonLabel,
  IonRouterOutlet,
  IonTabBar,
  IonTabButton,
  IonTabs,
  setupIonicReact,
} from "@ionic/react";
import { IonReactRouter } from "@ionic/react-router";
import { home, grid, settings } from "ionicons/icons";

/* Core CSS required for Ionic components to work properly */
import "@ionic/react/css/core.css";

/* Basic CSS for apps built with Ionic */
import "@ionic/react/css/normalize.css";
import "@ionic/react/css/structure.css";
import "@ionic/react/css/typography.css";

/* Optional CSS utils that can be commented out */
import "@ionic/react/css/padding.css";
import "@ionic/react/css/float-elements.css";
import "@ionic/react/css/text-alignment.css";
import "@ionic/react/css/text-transformation.css";
import "@ionic/react/css/flex-utils.css";
import "@ionic/react/css/display.css";

/**
 * Ionic Dark Mode
 */
import "@ionic/react/css/palettes/dark.class.css";

/* Theme variables */
import "./theme/variables.css";

/* Pages */
import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Settings from "./pages/Settings";

import { ThemeProvider } from "./contexts/ThemeContext";

setupIonicReact({
  mode: "md", // Use Material Design for consistent cross-platform look
});

function App() {
  return (
    <ThemeProvider>
      <IonApp>
        <IonReactRouter>
          <IonTabs>
            <IonRouterOutlet>
              <Route exact path="/home">
                <Home />
              </Route>
              <Route exact path="/dashboard">
                <Dashboard />
              </Route>
              <Route exact path="/settings">
                <Settings />
              </Route>
              <Route exact path="/">
                <Redirect to="/home" />
              </Route>
            </IonRouterOutlet>
            <IonTabBar slot="bottom">
              <IonTabButton tab="home" href="/home">
                <IonIcon icon={home} aria-hidden="true" />
                <IonLabel>Home</IonLabel>
              </IonTabButton>
              <IonTabButton tab="dashboard" href="/dashboard">
                <IonIcon icon={grid} aria-hidden="true" />
                <IonLabel>Dashboard</IonLabel>
              </IonTabButton>
              <IonTabButton tab="settings" href="/settings">
                <IonIcon icon={settings} aria-hidden="true" />
                <IonLabel>Settings</IonLabel>
              </IonTabButton>
            </IonTabBar>
          </IonTabs>
        </IonReactRouter>
      </IonApp>
    </ThemeProvider>
  );
}

export default App;
