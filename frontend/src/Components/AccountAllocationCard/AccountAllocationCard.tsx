import React from 'react';

type AccountAllocationItem = {
    id: string;
    accountName: string;
    statusText: string;
    isWarning: boolean;
    details: string;
};

type AccountAllocationCardProps = {
    accounts?: AccountAllocationItem[];
    onRebalance?: (accountName: string) => void;
};

const AccountAllocationCard = ({ accounts, onRebalance }: AccountAllocationCardProps) => {
    // Standardmock om inga props skickas med
    const defaultAccounts: AccountAllocationItem[] = [
        { id: '1', accountName: 'ISK', statusText: '⚠️ Avviker 15%', isWarning: true, details: 'Nuläge: 85% Aktier / 15% Fonder (Mål: 70/30)' },
        { id: '2', accountName: 'KF', statusText: 'Balanserat ✓', isWarning: false, details: 'Nuläge: 20% Aktier / 80% Fonder (Mål: 20/80)' },
        { id: '3', accountName: 'Depå', statusText: 'Balanserat ✓', isWarning: false, details: 'Nuläge: 50% Aktier / 50% Fonder (Mål: 50/50)' },
    ];

    const listToDisplay = accounts || defaultAccounts;

    return (
        <div style={styles.card}>
            <h3 style={styles.title}>Mina Konton & Målallokering</h3>
            <div style={styles.listContainer}>
                {listToDisplay.map((acc) => (
                    <div key={acc.id} style={styles.accountBox}>
                        <div style={styles.accountHeader}>
                            <span style={styles.accountName}>{acc.accountName}</span>
                            <span style={acc.isWarning ? styles.badgeWarning : styles.badgeSuccess}>
                                {acc.statusText}
                            </span>
                        </div>
                        <p style={styles.accountDetails}>{acc.details}</p>
                        {acc.isWarning && (
                            <button 
                                style={styles.rebalanceBtn}
                                onClick={() => onRebalance && onRebalance(acc.accountName)}
                            >
                                Rebalansera konto
                            </button>
                        )}
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
    accountHeader: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '6px',
    },
    accountName: {
        fontSize: '14px',
        fontWeight: 600,
        color: '#1E293B',
    },
    badgeWarning: {
        fontSize: '12px',
        fontWeight: 500,
        color: '#DC2626',
        backgroundColor: '#FEF2F2',
        padding: '2px 8px',
        borderRadius: '4px',
    },
    badgeSuccess: {
        fontSize: '12px',
        fontWeight: 500,
        color: '#059669',
        backgroundColor: '#ECFDF5',
        padding: '2px 8px',
        borderRadius: '4px',
    },
    accountDetails: {
        margin: '0 0 10px 0',
        fontSize: '13px',
        color: '#64748B',
    },
    rebalanceBtn: {
        padding: '6px 12px',
        borderRadius: '6px',
        border: 'none',
        backgroundColor: '#059669',
        color: '#FFFFFF',
        fontSize: '12px',
        fontWeight: 500,
        cursor: 'pointer',
        transition: 'background-color 0.2s ease',
    },
};