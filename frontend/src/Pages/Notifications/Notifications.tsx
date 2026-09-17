import { useState } from 'react';
import FilterButton from "../../Components/FilterButton/FilterButton";
import NotificationCard, { type NotificationCategory } from "../../Components/NotificationCard/NotificationCard";
import "./Notifications.css";

const Notiser = () => {
    const [activeFilter, setActiveFilter] = useState('Alla');

    const filters = ['Alla', 'Värdepapper', 'Utdelningar', 'Rapporter', 'Övrigt'];

    const notifications: {
        id: number;
        title: string;
        subtitle: string;
        timestamp: string;
        category: NotificationCategory;
        icon: string;
    }[] = [
        {
            id: 1,
            title: 'ISK- allokering avviker 8% från mål',
            subtitle: 'Övergänk ombalansering.',
            timestamp: 'Idag 08:02',
            category: 'Värdepapper',
            icon: '📈',
        },
        {
            id: 2,
            title: 'Utdelning Ericsson B',
            subtitle: '2 SEK per aktie',
            timestamp: 'Igår 10:42',
            category: 'Utdelningar',
            icon: '💰',
        },
        {
            id: 3,
            title: 'KF- allokering avviker 6% från mål',
            subtitle: '(25%).',
            timestamp: 'Igår 16:45',
            category: 'Värdepapper',
            icon: '📈',
        },
        {
            id: 4,
            title: 'Ny kvartalsrapport – Apple Inc',
            subtitle: 'Rapport för Q3 publicerad.',
            timestamp: 'Igår 14:32',
            category: 'Rapporter',
            icon: '📄',
        },
    ];

    const filteredNotifications = activeFilter === 'Alla'
        ? notifications
        : notifications.filter((n) => n.category === activeFilter);

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

            <div className="notifications-list">
                {filteredNotifications.map((notif) => (
                    <NotificationCard
                        key={notif.id}
                        title={notif.title}
                        subtitle={notif.subtitle}
                        timestamp={notif.timestamp}
                        category={notif.category}
                        icon={notif.icon}
                    />
                ))}
            </div>
        </div>
    );
};

export default Notiser;