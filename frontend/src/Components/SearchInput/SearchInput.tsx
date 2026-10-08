import React from 'react';

type SearchInputProps = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
};

const SearchInput = ({
    value,
    onChange,
    placeholder = 'Sök värdepapper...',
}: SearchInputProps) => {
    return (
        <div style={styles.container}>
            <span style={styles.icon}>
                🔍
            </span>
            <input
                type="text"
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                style={styles.input}
            />
        </div>
    );
};

export default SearchInput;

const styles: Record<string, React.CSSProperties> = {
    container: {
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        width: '100%',
        maxWidth: '380px',
        boxSizing: 'border-box',
    },
    icon: {
        position: 'absolute',
        left: '14px',
        fontSize: '16px',
        color: '#94A3B8',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        pointerEvents: 'none',
    },
    input: {
        width: '100%',
        padding: '10px 14px 10px 42px',
        backgroundColor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '12px',
        fontSize: '14px',
        color: '#1E293B',
        outline: 'none',
        boxSizing: 'border-box',
        transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
    },
};