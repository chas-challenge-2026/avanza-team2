import { useEffect, useState } from 'react';
import FilterButton from "../../Components/FilterButton/FilterButton";
import NotificationCard, {
    type NotificationCategory
} from "../../Components/NotificationCard/NotificationCard";
import "./Notifications.css";

const API_URL = import.meta.env.VITE_API_URL ?? '';
const POLL_INTERVAL_MS = 30_000;

interface StoredAlert {
    id: number;
    user: number;
    message: string;
    dismissed: boolean;
    createdAt: string;
}

interface LiveAlert {
    alertType: string;
    message: string;
    dismissed: boolean;
    createdAt: string;
}

interface AlertsResponse {
    storedAlerts: {
        content: StoredAlert[];
    };
    liveAlerts: LiveAlert[];
    driftThreshold: number;
}

interface Notification {
    id: string;
    title: string;
    subtitle: string;
    timestamp: string;
    category: NotificationCategory;
    icon: string;
}

const formatDate = (value: string) => {
    if (value === 'Nu') {
        return 'Nu';
    }

    const date = new Date(value.replace(' ', 'T'));

    return Number.isNaN(date.getTime())
        ? value
        : date.toLocaleDateString('sv-SE');
};

const Notiser = () => {
    const [activeFilter, setActiveFilter] = useState('Alla');
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [error, setError] = useState<string | null>(null);

    const filters = [
        'Alla',
        'Värdepapper',
        'Utdelningar',
        'Rapporter',
        'Övrigt'
    ];

    useEffect(() => {
        let active = true;

        const loadAlerts = async () => {
            try {
                const response = await fetch(`${API_URL}/api/alerts`, {
                    credentials: 'include',
                });

                if (!response.ok) {
                    throw new Error(`Alerts request failed: ${response.status}`);
                }

                const data = (await response.json()) as AlertsResponse;

                if (!active) {
                    return;
                }

                const storedNotifications: Notification[] =
                    data.storedAlerts.content
                        .filter((alert) => !alert.dismissed)
                        .map((alert) => ({
                            id: `stored-${alert.id}`,
                            title: alert.message,
                            subtitle: 'Varning',
                            timestamp: formatDate(alert.createdAt),
                            category: 'Övrigt',
                            icon: '⚠️',
                        }));

                const liveNotifications: Notification[] =
                    data.liveAlerts
                        .filter((alert) => !alert.dismissed)
                        .map((alert, index) => ({
                            id: `live-${index}-${alert.alertType}`,
                            title: alert.message,
                            subtitle: 'Live-varning',
                            timestamp: alert.createdAt,
                            category: 'Värdepapper',
                            icon: '📈',
                        }));

                setNotifications([
                    ...liveNotifications,
                    ...storedNotifications,
                ]);

                setError(null);
            } catch  {
                if (active) {
                    setError('Kunde inte hämta notiser.');
                }
            }
        };

        void loadAlerts();

        const intervalId = window.setInterval(
            () => void loadAlerts(),
            POLL_INTERVAL_MS
        );

        return () => {
            active = false;
            window.clearInterval(intervalId);
        };
    }, []);

    const filteredNotifications =
        activeFilter === 'Alla'
            ? notifications
            : notifications.filter(
                (notification) => notification.category === activeFilter
            );

    return (
        <div className="notiser-container">
            <h1 className="notiser-title">Notiser</h1>

            <div className="filter-group">
                {filters.map((filter) => (
                    <FilterButton
                        key={filter}
                        label={filter}
                        active={activeFilter === filter}
                        onClick={() => setActiveFilter(filter)}
                    />
                ))}
            </div>

            <h3 className="section-title">Senaste notiser</h3>

            {error && (
                <p className="text-sm text-red-600">
                    {error}
                </p>
            )}

            <div className="notifications-list">
                {filteredNotifications.map((notification) => (
                    <NotificationCard
                        key={notification.id}
                        title={notification.title}
                        subtitle={notification.subtitle}
                        timestamp={notification.timestamp}
                        category={notification.category}
                        icon={notification.icon}
                    />
                ))}
            </div>
        </div>
    );
};

export default Notiser;