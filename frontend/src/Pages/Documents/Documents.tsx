import { useState } from 'react';
import DocumentFilter from '../../Components/DocumentFilter/DocumentFilter';
import DocumentCard from '../../Components/DocumentCard/DocumentCard';

// Mockdata
const MOCK_DOCUMENTS = [
    { id: '1', title: 'Årsbesked ISK 2025', date: '2026-01-15', category: 'Årsbesked', fileSize: '1.2 MB' },
    { id: '2', title: 'Transaktionshistorik Q1', date: '2026-04-01', category: 'Transaktioner', fileSize: '450 KB' },
    { id: '3', title: 'Utdelningsspecifikation 2025', date: '2026-01-20', category: 'Utdelningar', fileSize: '820 KB' },
    { id: '4', title: 'Årsbesked Fondkonto 2025', date: '2026-01-15', category: 'Årsbesked', fileSize: '950 KB' },
    { id: '5', title: 'Transaktionshistorik Q4 2025', date: '2026-01-02', category: 'Transaktioner', fileSize: '1.1 MB' },
];

export const Documents = () => {
    const [activeFilter, setActiveFilter] = useState('Alla');

    const filteredDocuments = MOCK_DOCUMENTS.filter((doc) => {
        if (activeFilter === 'Alla') return true;
        return doc.category === activeFilter;
    });

    const handleDownload = (id: string) => {
        console.log('Laddar ner dokument med id:', id);
    };

    return (
        <div style={styles.container}>
            <div style={styles.headerWrapper}>
                <h2 style={styles.headerTitle}>Dokument & Historik</h2>
                <p style={styles.subtitle}>Här hittar du dina personliga årsbesked, transaktioner och utdelningar.</p>
            </div>

            <div style={styles.filterWrapper}>
                <DocumentFilter activeFilter={activeFilter} setActiveFilter={setActiveFilter} />
            </div>
            
            <div style={styles.listContainer}>
                {filteredDocuments.length > 0 ? (
                    filteredDocuments.map((doc) => (
                        <DocumentCard key={doc.id} item={doc} onDownload={handleDownload} />
                    ))
                ) : (
                    <div style={styles.emptyState}>
                        <p style={styles.emptyText}>Inga dokument att visa för detta filter.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

const styles: Record<string, React.CSSProperties> = {
    container: {
        padding: '24px 16px',
        maxWidth: '900px',
        width: '100%',
        margin: '0 auto', 
        boxSizing: 'border-box',
    },
    headerWrapper: {
        marginBottom: '24px',
        textAlign: 'center', 
    },
    headerTitle: {
        margin: 0,
        fontSize: '26px',
        fontWeight: 700,
        color: '#0F172A',
    },
    subtitle: {
        margin: '6px 0 0 0',
        fontSize: '14px',
        color: '#64748B',
    },
    filterWrapper: {
        display: 'flex',
        justifyContent: 'center', 
        marginBottom: '24px',
        width: '100%',
    },
    listContainer: {
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        width: '100%',
    },
    emptyState: {
        textAlign: 'center',
        padding: '48px 16px',
        backgroundColor: '#F8FAFC',
        borderRadius: '12px',
        border: '1px dashed #CBD5E1',
    },
    emptyText: {
        margin: 0,
        color: '#64748B',
        fontSize: '14px',
    },
};