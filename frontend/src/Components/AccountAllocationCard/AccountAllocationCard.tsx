import React from 'react';

type AllocationSplit = {
    aktier: number;
    fonder: number;
};

type AccountAllocationItem = {
    id: string;
    accountName: string;
    subtitle?: string;
    isWarning: boolean;
    target: AllocationSplit;
    actual: AllocationSplit;
    valueSek: number;
};

type AccountAllocationCardProps = {
    accounts?: AccountAllocationItem[];
    onRebalance?: (accountId: string) => void;
};

const AccountAllocationCard = ({ accounts, onRebalance }: AccountAllocationCardProps) => {
    // Standardmock om inga props skickas med
    const defaultAccounts: AccountAllocationItem[] = [
        {
            id: '1',
            accountName: 'ISK 1',
            subtitle: 'Huvudkonto',
            isWarning: true,
            target: { aktier: 70, fonder: 30 },
            actual: { aktier: 82, fonder: 18 },
            valueSek: 120000,
        },
        {
            id: '2',
            accountName: 'ISK 2',
            subtitle: 'Långsiktigt',
            isWarning: false,
            target: { aktier: 60, fonder: 40 },
            actual: { aktier: 59, fonder: 41 },
            valueSek: 45000,
        },
        {
            id: '3',
            accountName: 'KF',
            subtitle: 'Pension',
            isWarning: false,
            target: { aktier: 40, fonder: 60 },
            actual: { aktier: 41, fonder: 59 },
            valueSek: 30000,
        },
    ];

    const listToDisplay = accounts ?? defaultAccounts;

    const formatSek = (value: number): string =>
        `${value.toLocaleString('sv-SE', { maximumFractionDigits: 0 })} SEK`;

    return (
        <div style={styles.card}>
            <h3 style={styles.title}>Mina Konton & Målallokering</h3>
            <div style={styles.listContainer}>
                {listToDisplay.length === 0 && (
                    <p style={styles.emptyText}>Kunde inte hämta kontofördelningen just nu.</p>
                )}
                {listToDisplay.map((acc) => (
                    <div
                        key={acc.id}
                        style={acc.isWarning ? styles.accountBoxWarning : styles.accountBox}
                    >
                        <div style={styles.accountHeader}>
                            <div style={styles.accountNameGroup}>
                                <span style={styles.accountName}>
                                    {acc.accountName}
                                    {acc.subtitle && (
                                        <span style={styles.accountSubtitle}> ({acc.subtitle})</span>
                                    )}
                                </span>
                                <span style={acc.isWarning ? styles.badgeWarning : styles.badgeSuccess}>
                                    <span
                                        style={{
                                            ...styles.badgeDot,
                                            backgroundColor: acc.isWarning ? '#DC2626' : '#059669',
                                        }}
                                    />
                                    {acc.isWarning ? (
                                        <strong>
                                            Avviker {Math.abs(acc.actual.aktier - acc.target.aktier)}%
                                        </strong>
                                    ) : (
                                        'I balans'
                                    )}
                                </span>
                            </div>
                            {acc.isWarning ? (
                                <button
                                    style={styles.rebalanceBtn}
                                    onClick={() => onRebalance && onRebalance(acc.id)}
                                >
                                    Rebalansera
                                </button>
                            ) : (
                                <span style={styles.valueText}>{formatSek(acc.valueSek)}</span>
                            )}
                        </div>

                        <div style={styles.splitRow}>
                            <strong style={styles.splitLabel}>
                                Mål: {acc.target.aktier}% Aktier / {acc.target.fonder}% Fonder
                            </strong>
                            <strong style={acc.isWarning ? styles.splitActualWarning : styles.splitActual}>
                                {acc.isWarning ? 'Nuvarande' : 'Faktisk'}: {acc.actual.aktier}% Aktier /{' '}
                                {acc.actual.fonder}% Fonder
                            </strong>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AccountAllocationCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        backgroundColor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '12px',
        padding: '20px',
        boxSizing: 'border-box',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
    },
    title: {
        margin: '0 0 16px 0',
        fontSize: '16px',
        fontWeight: 600,
        color: '#0F172A',
    },
    listContainer: {
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
    },
    accountBox: {
        padding: '14px',
        backgroundColor: '#F8FAFC',
        borderRadius: '8px',
        border: '1px solid #E2E8F0',
    },
    accountBoxWarning: {
        padding: '14px',
        backgroundColor: '#FFFBEB',
        borderRadius: '8px',
        border: '1px solid #FDE68A',
    },
    accountHeader: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '8px',
        gap: '12px',
    },
    accountNameGroup: {
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        flexWrap: 'wrap',
    },
    accountName: {
        fontSize: '14px',
        fontWeight: 600,
        color: '#1E293B',
    },
    accountSubtitle: {
        fontSize: '13px',
        fontWeight: 400,
        color: '#94A3B8',
    },
    badgeDot: {
        display: 'inline-block',
        width: '6px',
        height: '6px',
        borderRadius: '50%',
    },
    badgeWarning: {
        display: 'inline-flex',
        alignItems: 'center',
        gap: '5px',
        fontSize: '12px',
        fontWeight: 500,
        color: '#B45309',
        backgroundColor: '#FEF3C7',
        padding: '2px 8px',
        borderRadius: '999px',
    },
    badgeSuccess: {
        display: 'inline-flex',
        alignItems: 'center',
        gap: '5px',
        fontSize: '12px',
        fontWeight: 500,
        color: '#059669',
        backgroundColor: '#ECFDF5',
        padding: '2px 8px',
        borderRadius: '999px',
    },
    valueText: {
        fontSize: '13px',
        fontWeight: 600,
        color: '#94A3B8',
        whiteSpace: 'nowrap',
    },
    splitRow: {
        display: 'flex',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '8px',
        fontSize: '13px',
        marginBottom: '8px',
    },
    splitLabel: {
        color: '#64748B',
    },
    splitActual: {
        color: '#1E293B',
    },
    splitActualWarning: {
        color: '#B45309',
    },
    emptyText: {
        margin: 0,
        fontSize: '13px',
        color: '#64748B',
    },
    rebalanceBtn: {
        padding: '8px 16px',
        borderRadius: '6px',
        border: 'none',
        backgroundColor: '#059669',
        color: '#FFFFFF',
        fontSize: '12px',
        fontWeight: 600,
        cursor: 'pointer',
        whiteSpace: 'nowrap',
        transition: 'background-color 0.2s ease',
    },
};
