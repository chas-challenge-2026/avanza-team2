
type FilterButtonProps = {
    label: string;
    active: boolean;
    onClick: () => void;
};

const FilterButton = ({ label, active, onClick }: FilterButtonProps) => {
    return (
        <button
            onClick={onClick}
            style={{
                ...styles.button,
                ...(active ? styles.activeButton : styles.inactiveButton),
            }}
        >
            {label}
        </button>
    );
};

export default FilterButton;

const styles: Record<string, React.CSSProperties> = {
    button: {
        padding: '8px 20px',
        borderRadius: '20px',
        fontSize: '14px',
        fontWeight: 500,
        cursor: 'pointer',
        border: '1px solid #E2E8F0',
        transition: 'all 0.2s ease',
        outline: 'none',
    },
    activeButton: {
        backgroundColor: '#D1FAE5',
        color: '#065F46',
        borderColor: '#A7F3D0',
        fontWeight: 600,
    },
    inactiveButton: {
        backgroundColor: '#F8FAFC',
        color: '#64748B',
        borderColor: '#E2E8F0',
    },
};