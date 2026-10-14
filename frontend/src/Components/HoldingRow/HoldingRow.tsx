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

// Formats numbers using Swedish number formatting.
const formatNumber = (value: number): string =>
    value.toLocaleString('sv-SE', {
        maximumFractionDigits: 2,
    });

// Formats percentage returns with two decimal places and the correct sign.
const formatReturn = (value: number): string => {
    const formattedValue = value.toLocaleString('sv-SE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    });

    return value >= 0
        ? `+${formattedValue}%`
        : `${formattedValue}%`;
};

const HoldingRow = ({ item, onClick }: HoldingRowProps) => {
    // Use green for positive returns and red for negative returns.
    const returnColor =
        item.returnPercent >= 0 ? '#16A34A' : '#DC2626';

    return (
                <div
                    style={styles.row}
                    onClick={() => onClick && onClick(item.id)}
                    >
                    <div style={styles.leftContent}>
                <div style={styles.iconContainer}>
                    <span>📈</span>
                </div>

                <div style={styles.stockInfo}>
                    <h4 style={styles.symbol}>{item.symbol}</h4>
                    <p style={styles.name}>{item.name}</p>
                </div>
            </div>

            <div style={styles.rightContent}>
                {/* Display the number of shares as a whole number when applicable. */}
                <span style={styles.text}>
                    {formatNumber(item.shares)}
                </span>

                {/* Format the market value using Swedish number formatting. */}
                <span style={styles.text}>
                    {formatNumber(item.value)} SEK
                </span>

                {/* Display the percentage return with two decimal places. */}
                <span
                    style={{
                        ...styles.return,
                        color: returnColor,
                    }}
                >
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
        display: 'grid',
        gridTemplateColumns: '40px 1fr',
        alignItems: 'center',
        columnGap: '14px',
        flex: '1',
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
        width: '75px',
        textAlign: 'right',
        whiteSpace: 'nowrap',
    },
    arrow: {
        fontSize: '18px',
        color: '#94A3B8',
    },
};