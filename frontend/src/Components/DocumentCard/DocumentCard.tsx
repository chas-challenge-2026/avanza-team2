type DocumentItem = {
    id: string;
    title: string;
    date: string;
    category: string;
    fileSize?: string;
};

type DocumentCardProps = {
    item: DocumentItem;
    onDownload?: (id: string) => void;
};

const DocumentCard = ({ item, onDownload }: DocumentCardProps) => {
    return (
        <div style={styles.card}>
            <div style={styles.leftContent}>
                <div style={styles.iconContainer}>
                    <span>📄</span>
                </div>
                <div>
                    <h4 style={styles.title}>{item.title}</h4>
                    <p style={styles.subtitle}>
                        {item.date} • <span style={styles.categoryBadge}>{item.category}</span>
                        {item.fileSize && ` • ${item.fileSize}`}
                    </p>
                </div>
            </div>
            <button 
                style={styles.downloadBtn} 
                onClick={() => onDownload && onDownload(item.id)}
            >
                Ladda ner
            </button>
        </div>
    );
};

export default DocumentCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '14px 18px',
        backgroundColor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '12px',
        marginBottom: '10px',
        boxSizing: 'border-box',
        transition: 'box-shadow 0.2s ease',
    },
    leftContent: {
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
    },
    iconContainer: {
        width: '40px',
        height: '40px',
        borderRadius: '8px',
        backgroundColor: '#F1F5F9',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '18px',
    },
    title: {
        margin: 0,
        fontSize: '14px',
        fontWeight: 600,
        color: '#1E293B',
    },
    subtitle: {
        margin: '4px 0 0 0',
        fontSize: '12px',
        color: '#64748B',
    },
    categoryBadge: {
        fontWeight: 500,
        color: '#475569',
    },
    downloadBtn: {
        padding: '6px 14px',
        borderRadius: '6px',
        border: '1px solid #CBD5E1',
        backgroundColor: '#FFFFFF',
        color: '#334155',
        fontSize: '12px',
        fontWeight: 500,
        cursor: 'pointer',
        transition: 'background-color 0.2s ease',
    },
};