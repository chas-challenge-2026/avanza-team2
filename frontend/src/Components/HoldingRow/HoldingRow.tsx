import React from 'react';

export type HoldingItem = {
    id: string;
    symbol: string;
    name: string;
    shares: number;
    value: number;
    returnPercent: number;
};

type HoldingRowProps = {
    item: HoldingItem;
    onClick?: (id: string) => void;
};

const HoldingRow = ({ item, onClick }: HoldingRowProps) => {
    return (
        <div style={styles.row} onClick={() => onClick && onClick(item.id)}>
            <div style={styles.leftContent}>
                <div style={styles.iconContainer}>
                    <span>📈</span>
                </div>
                <div>
                    <h4 style={styles.symbol}>{item.symbol}</h4>
                    <p style={styles.name}>{item.name}</p>
                </div>
            </div>

            <div style={styles.rightContent}>
                <span style={styles.text}>{item.shares}</span>
                <span style={styles.text}>{item.value.toLocaleString('sv-SE')} SEK</span>
                <span style={{ 
                    ...styles.return, 
                    color: item.returnPercent >= 0 ? '#16A34A' : '#DC2626' 
                }}>
                    {item.returnPercent >= 0 ? `+${item.returnPercent}%` : `${item.returnPercent}%`}
                </span>
                <span style={styles.arrow}>›</span>
            </div>
        </div>
    );
};

export default HoldingRow;

const styles: Record<string, React.CSSProperties> = {
    row: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '16px 20px',
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #F1F5F9',
        cursor: 'pointer',
        boxSizing: 'border-box',
        transition: 'background-color 0.2s ease',
    },
    leftContent: {
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
    },
    iconContainer: {
        width: '40px',
        height: '40px',
        borderRadius: '10px',
        backgroundColor: '#EFF6FF',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '18px',
    },
    symbol: {
        margin: 0,
        fontSize: '14px',
        fontWeight: 700,
        color: '#0F172A',
    },
    name: {
        margin: '2px 0 0 0',
        fontSize: '12px',
        color: '#64748B',
    },
    rightContent: {
        display: 'flex',
        alignItems: 'center',
        gap: '24px',
    },
    text: {
        fontSize: '14px',
        fontWeight: 500,
        color: '#334155',
    },
    return: {
        fontSize: '14px',
        fontWeight: 600,
        width: '60px',
        textAlign: 'right',
    },
    arrow: {
        fontSize: '18px',
        color: '#94A3B8',
    },
};