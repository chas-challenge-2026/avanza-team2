type DocumentFilterProps = {
    activeFilter: string;
    setActiveFilter: (filter: string) => void;
};

const DocumentFilter = ({ activeFilter, setActiveFilter }: DocumentFilterProps) => {
    const filters = ['Alla', 'Årsbesked', 'Transaktioner', 'Utdelningar'];

    return (
        <div style={styles.container}>
            <div style={styles.filterButtons}>
                {filters.map((filter) => {
                    const isActive = activeFilter === filter;
                    return (
                        <button
                            key={filter}
                            style={{
                                ...styles.filterBtn,
                                ...(isActive ? styles.activeFilterBtn : {}),
                            }}
                            onClick={() => setActiveFilter(filter)}
                        >
                            {filter}
                        </button>
                    );
                })}
            </div>
        </div>
    );
};

export default DocumentFilter;

const styles: Record<string, React.CSSProperties> = {
    container: {
        display: 'flex',
        justifyContent: 'center', 
        alignItems: 'center',
        marginBottom: '24px',
        width: '100%',
        boxSizing: 'border-box',
    },
    filterButtons: {
        display: 'flex',
        flexWrap: 'wrap', 
        justifyContent: 'center',
        gap: '8px',
    },
    filterBtn: {
        padding: '6px 16px',
        borderRadius: '20px',
        border: '1px solid #E2E8F0',
        backgroundColor: '#F8FAFC',
        cursor: 'pointer',
        fontSize: '13px',
        fontWeight: 500,
        color: '#475569',
        transition: 'all 0.2s ease',
    },
    activeFilterBtn: {
        backgroundColor: '#DCFCE7',
        borderColor: '#22C55E',
        color: '#15803D',
    },
};