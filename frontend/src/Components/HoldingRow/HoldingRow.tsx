import React from 'react';
import './HoldingRow.css';

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

const formatShares = (value: number): string =>
    value.toLocaleString('sv-SE', {
        maximumFractionDigits: 0,
    });

const formatSek = (value: number): string =>
    value.toLocaleString('sv-SE', {
        maximumFractionDigits: 0,
    });

const formatReturn = (value: number): string => {
    const formattedValue = value.toLocaleString('sv-SE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    });

    return value >= 0 ? `+${formattedValue}%` : `${formattedValue}%`;
};

const HoldingRow = ({ item, onClick }: HoldingRowProps) => {
    const returnColor = item.returnPercent >= 0 ? '#16A34A' : '#DC2626';

    return (
        <div style={styles.row} onClick={() => onClick?.(item.id)}>
            <div style={styles.leftContent} className="holding-row-left">
                <div style={styles.iconContainer}>
                    <span>📈</span>
                </div>
                <div style={styles.stockInfo} className="holding-row-text">
                    <h4 style={styles.symbol} className="holding-row-symbol">
                        {item.symbol}
                    </h4>
                    <p style={styles.name} className="holding-row-name">
                        {item.name}
                    </p>
                </div>
            </div>

            <div style={styles.rightContent} className="holding-row-right">
                <span style={styles.text} className="holding-row-shares">
                    {formatShares(item.shares)} st.
                </span>
                <span style={styles.text}>{formatSek(item.value)} SEK</span>
                <span style={{ ...styles.return, color: returnColor }}>
                    {formatReturn(item.returnPercent)}
                </span>
                <span style={styles.arrow}>›</span>
            </div>
        </div>
    );
};

export default HoldingRow;

const styles: Record<string, React.CSSProperties> = {
    row: {
        display: 'grid',
        gridTemplateColumns: 'minmax(0, 1fr) minmax(42px, auto) minmax(76px, auto) minmax(65px, auto) 12px',
        columnGap: '8px',
        alignItems: 'center',
        padding: '16px 20px',
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #F1F5F9',
        cursor: 'pointer',
        boxSizing: 'border-box',
        transition: 'background-color 0.2s ease',
    },
    leftContent: {
        display: 'grid',
        gridTemplateColumns: '40px 1fr',
        alignItems: 'center',
        columnGap: '14px',
        minWidth: 0,
    },
    stockInfo: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        minWidth: 0,
    },
    iconContainer: {
        width: '40px',
        height: '40px',
        boxSizing: 'border-box',
        borderRadius: '10px',
        backgroundColor: '#EFF6FF',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '18px',
        flexShrink: 0,
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
        display: 'contents',
    },
    text: {
        fontSize: '14px',
        fontWeight: 500,
        color: '#334155',
        textAlign: 'right',
    },
    return: {
        fontSize: '14px',
        fontWeight: 600,
        width: '75px',
        textAlign: 'right',
        whiteSpace: 'nowrap',
    },
    arrow: {
        fontSize: '18px',
        color: '#94A3B8',
    },
};