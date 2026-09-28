export type NotificationCategory = 'Alla' | 'Värdepapper' | 'Utdelningar' | 'Rapporter' | 'Övrigt';

type NotificationCardProps = {
    title: string;
    subtitle: string;
    timestamp: string;
    category: NotificationCategory;
    icon: string;
};

const NotificationCard = ({
    title,
    subtitle,
    timestamp,
    icon
}: NotificationCardProps) => {

    return (
        <div style={styles.card}>
            <span style={styles.icon}>{icon}</span>

            <div style={styles.content}>
                <h4 style={styles.title}>{title}</h4>
                <p style={styles.subtitle}>{subtitle}</p>
            </div>

            <span style={styles.time}>{timestamp}</span>
        </div>
    );
};

export default NotificationCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between', 
        gap: '12px',
        backgroundColor: '#FFFFFF',
        borderRadius: '8px',
        padding: '12px 16px',
        margin: '8px 0',
        border: '1px solid #E2E8F0',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
        width: '100%',
        boxSizing: 'border-box',
    },
    icon: {
        fontSize: '18px',
        lineHeight: 1,
    },
    content: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center', 
        textAlign: 'center',
        gap: '2px',
        minWidth: 0,
    },
    title: {
        margin: 0,
        fontSize: '13px',
        fontWeight: 600,
        color: 'black',
    },
    subtitle: {
        margin: 0,
        fontSize: '12px',
        color: 'black',
        lineHeight: 1.3,
    },
    time: {
        fontSize: '11px',
        color: '#94A3B8',
        whiteSpace: 'nowrap',
    },
};