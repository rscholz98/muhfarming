import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonButton,
  IonIcon,
  IonGrid,
  IonRow,
  IonCol,
  IonFab,
  IonFabButton,
} from "@ionic/react";
import { add, statsChart, calendar, notifications } from "ionicons/icons";
import "./Home.css";

const Home = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>MuhFarming</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        {/* Stats Overview */}
        <IonGrid>
          <IonRow>
            <IonCol size="6">
              <IonCard className="stat-card">
                <IonCardHeader>
                  <IonIcon icon={statsChart} className="stat-icon" />
                  <IonCardTitle className="stat-value">24</IonCardTitle>
                </IonCardHeader>
                <IonCardContent>
                  <p className="stat-label">Active Tasks</p>
                </IonCardContent>
              </IonCard>
            </IonCol>
            <IonCol size="6">
              <IonCard className="stat-card">
                <IonCardHeader>
                  <IonIcon icon={calendar} className="stat-icon" />
                  <IonCardTitle className="stat-value">7</IonCardTitle>
                </IonCardHeader>
                <IonCardContent>
                  <p className="stat-label">Due Today</p>
                </IonCardContent>
              </IonCard>
            </IonCol>
          </IonRow>
        </IonGrid>

        {/* Recent Activity */}
        <h2 className="section-title">Recent Activity</h2>

        <IonCard>
          <IonCardHeader>
            <IonCardTitle>Field Inspection</IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <p>
              Completed inspection of north field. All crops looking healthy.
            </p>
            <IonButton fill="clear" size="small">
              View Details
            </IonButton>
          </IonCardContent>
        </IonCard>

        <IonCard>
          <IonCardHeader>
            <IonCardTitle>Irrigation Schedule</IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <p>Updated irrigation schedule for the upcoming week.</p>
            <IonButton fill="clear" size="small">
              View Details
            </IonButton>
          </IonCardContent>
        </IonCard>

        <IonCard>
          <IonCardHeader>
            <IonCardTitle>Weather Alert</IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <p>
              Rain expected tomorrow. Adjust outdoor activities accordingly.
            </p>
            <IonButton fill="clear" size="small">
              View Details
            </IonButton>
          </IonCardContent>
        </IonCard>

        {/* Floating Action Button */}
        <IonFab slot="fixed" vertical="bottom" horizontal="end">
          <IonFabButton>
            <IonIcon icon={add} />
          </IonFabButton>
        </IonFab>
      </IonContent>
    </IonPage>
  );
};

export default Home;
