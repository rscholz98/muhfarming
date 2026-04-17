import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardSubtitle,
  IonCardContent,
  IonProgressBar,
  IonChip,
  IonIcon,
  IonBadge,
} from "@ionic/react";
import { trendingUp, water, sunny, leaf } from "ionicons/icons";
import "./Dashboard.css";

const Dashboard = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Dashboard</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        {/* Weather Card */}
        <IonCard className="weather-card">
          <IonCardHeader>
            <IonCardSubtitle>Current Weather</IonCardSubtitle>
            <IonCardTitle className="weather-title">
              <IonIcon icon={sunny} className="weather-icon" />
              <span>24°C</span>
            </IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <div className="weather-details">
              <IonChip>
                <IonIcon icon={water} />
                Humidity: 65%
              </IonChip>
              <IonChip>
                <IonIcon icon={leaf} />
                Wind: 12 km/h
              </IonChip>
            </div>
          </IonCardContent>
        </IonCard>

        {/* Crop Health */}
        <IonCard>
          <IonCardHeader>
            <IonCardSubtitle>Crop Health Overview</IonCardSubtitle>
            <IonCardTitle>Field Status</IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <div className="progress-item">
              <div className="progress-label">
                <span>North Field</span>
                <IonBadge color="success">Healthy</IonBadge>
              </div>
              <IonProgressBar value={0.92} color="success" />
            </div>
            <div className="progress-item">
              <div className="progress-label">
                <span>South Field</span>
                <IonBadge color="warning">Needs Attention</IonBadge>
              </div>
              <IonProgressBar value={0.68} color="warning" />
            </div>
            <div className="progress-item">
              <div className="progress-label">
                <span>East Field</span>
                <IonBadge color="success">Healthy</IonBadge>
              </div>
              <IonProgressBar value={0.85} color="success" />
            </div>
          </IonCardContent>
        </IonCard>

        {/* Quick Stats */}
        <IonCard>
          <IonCardHeader>
            <IonCardSubtitle>This Week</IonCardSubtitle>
            <IonCardTitle>
              <IonIcon icon={trendingUp} /> Performance
            </IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <div className="quick-stats">
              <div className="stat">
                <span className="stat-number">156</span>
                <span className="stat-text">Tasks Completed</span>
              </div>
              <div className="stat">
                <span className="stat-number">23</span>
                <span className="stat-text">Inspections</span>
              </div>
              <div className="stat">
                <span className="stat-number">8</span>
                <span className="stat-text">Alerts Resolved</span>
              </div>
            </div>
          </IonCardContent>
        </IonCard>
      </IonContent>
    </IonPage>
  );
};

export default Dashboard;
