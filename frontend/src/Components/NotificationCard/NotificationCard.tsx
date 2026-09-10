export type NotificationCategory = 'increase' | 'decrease' | 'report' | 'dividend';

type NotificationCardProps = {
    title: string;
    subtitle: string;
    timestamp: string;
    category: NotificationCategory;
};

const NotificationCard = ({
    category,
    title,
    subtitle,
    timestamp
}: NotificationCardProps) => {

    return (
        <div style={styles.card}>
            <span style={styles.icon}>:chart_with_upwards_trend:</span>

            <div style={styles.content}>
                <div style={styles.header}>
                    <h4 style={styles.title}>{title}</h4>
                    <span style={styles.time}>{timestamp}</span>
                </div>
                <p style={styles.subtitle}>{subtitle}</p>
            </div>
        </div>
    );
};

export default NotificationCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        display: 'flex',
        alignItems: 'flex-start',
        gap: '12px',
        backgroundColor: '#FFFFFF',
        borderRadius: '8px',
        padding: '10px 14px',
        margin: '8px 0',
        border: '1px solid #E2E8F0',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
        width: '400px',
        boxSizing: 'border-box',
    },
    icon: {
        fontSize: '18px',
        lineHeight: 1,
        paddingTop: '2px',
    },
    content: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        gap: '2px',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    title: {
        margin: 0,
        fontSize: '13px',
        fontWeight: 600,
        color: 'black',
    },
    time: {
        fontSize: '11px',
        color: '#94A3B8',
        whiteSpace: 'nowrap',
    },
    subtitle: {
        margin: 0,
        fontSize: '12px',
        color: 'black',
        lineHeight: 1.3,
    },
};