import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonList,
  IonItem,
  IonLabel,
  IonToggle,
  IonIcon,
  IonNote,
} from "@ionic/react";
import {
  moon,
  notifications,
  person,
  helpCircle,
  informationCircle,
  logOut,
} from "ionicons/icons";
import { useTheme } from "../contexts/ThemeContext";
import "./Settings.css";

const Settings = () => {
  const { isDark, toggleTheme } = useTheme();

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Settings</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        {/* Appearance Section */}
        <IonList inset>
          <IonItem>
            <IonIcon icon={moon} slot="start" />
            <IonLabel>Dark Mode</IonLabel>
            <IonToggle checked={isDark} onIonChange={toggleTheme} slot="end" />
          </IonItem>
        </IonList>

        {/* Notifications Section */}
        <IonList inset>
          <IonItem>
            <IonIcon icon={notifications} slot="start" />
            <IonLabel>
              <h2>Push Notifications</h2>
              <p>Enable push notifications</p>
            </IonLabel>
            <IonToggle slot="end" checked />
          </IonItem>
        </IonList>

        {/* Account Section */}
        <IonList inset>
          <IonItem button detail>
            <IonIcon icon={person} slot="start" />
            <IonLabel>Account</IonLabel>
          </IonItem>
        </IonList>

        {/* Support Section */}
        <IonList inset>
          <IonItem button detail>
            <IonIcon icon={helpCircle} slot="start" />
            <IonLabel>Help & Support</IonLabel>
          </IonItem>
          <IonItem button detail>
            <IonIcon icon={informationCircle} slot="start" />
            <IonLabel>About</IonLabel>
            <IonNote slot="end">v1.0.0</IonNote>
          </IonItem>
        </IonList>

        {/* Logout Section */}
        <IonList inset>
          <IonItem button detail={false} className="logout-item">
            <IonIcon icon={logOut} slot="start" color="danger" />
            <IonLabel color="danger">Log Out</IonLabel>
          </IonItem>
        </IonList>
      </IonContent>
    </IonPage>
  );
};

export default Settings;
