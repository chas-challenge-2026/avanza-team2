import { useState } from 'react';
import { updateTargetAllocations, type TargetAllocation } from '../../Services/targetAllocationService';

type AllocationSplit = {
    aktier: number;
    fonder: number;
};

type RebalanceModalProps = {
    accountType: string;
    accountLabel: string;
    actualPct: number;
    allocations: TargetAllocation[];
    assetSplit: { target: AllocationSplit; actual: AllocationSplit };
    onClose: () => void;
    onSaved: (updated: TargetAllocation[]) => void;
};

// drift on the first "other" account so the total is always exactly 100.
const redistribute = (
    allocations: TargetAllocation[],
    accountType: string,
    newValue: number,
): { accountType: string; targetPercentage: number }[] => {
    const others = allocations.filter((a) => a.accountType !== accountType);
    const remaining = 100 - newValue;
    const othersTotal = others.reduce((sum, a) => sum + a.targetPercentage, 0);

    const redistributed = others.map((a) => {
        const weight = othersTotal > 0 ? a.targetPercentage / othersTotal : 1 / others.length;
        return {
            accountType: a.accountType,
            targetPercentage: Math.round(remaining * weight * 100) / 100,
        };
    });

    const sum = newValue + redistributed.reduce((total, a) => total + a.targetPercentage, 0);
    const drift = Math.round((100 - sum) * 100) / 100;

    if (redistributed.length > 0 && drift !== 0) {
        redistributed[0].targetPercentage = Math.round((redistributed[0].targetPercentage + drift) * 100) / 100;
    }

    return [{ accountType, targetPercentage: newValue }, ...redistributed];
};

const RebalanceModal = ({
    accountType,
    accountLabel,
    actualPct,
    allocations,
    assetSplit,
    onClose,
    onSaved,
}: RebalanceModalProps) => {
    const originalTarget =
        allocations.find((a) => a.accountType === accountType)?.targetPercentage ?? actualPct;

    const [value, setValue] = useState(originalTarget);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const diff = actualPct - value;
    const matchesActual = Math.abs(diff) < 0.5;

    const handleSave = async () => {
        setSaving(true);
        setError(null);

        try {
            const payload = redistribute(allocations, accountType, value);
            const updated = await updateTargetAllocations(payload);
            onSaved(updated);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Kunde inte spara ny allokering.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div style={styles.overlay}>
            <div style={styles.card}>
                <div style={styles.header}>
                    <span style={styles.headerTitle}>Snabb rebalansering</span>
                    <button style={styles.closeBtn} onClick={onClose} aria-label="Stäng">
                        ×
                    </button>
                </div>

                <div style={styles.body}>
                    <h3 style={styles.heading}>Ditt {accountLabel} avviker!</h3>

                    <div style={styles.assetSplitBox}>
                        <p style={styles.assetSplitRow}>
                            <strong>
                                Mål: {assetSplit.target.aktier}% Aktier / {assetSplit.target.fonder}% Fonder
                            </strong>
                        </p>
                        <p style={styles.assetSplitRow}>
                            <strong>
                                Nuvarande: {assetSplit.actual.aktier}% Aktier / {assetSplit.actual.fonder}% Fonder
                            </strong>
                        </p>
                    </div>

                    <div style={styles.pillRow}>
                        <div style={styles.pill}>
                            <span style={styles.pillLabel}>Nuläge</span>
                            <span style={styles.pillValue}>{actualPct.toFixed(1)}%</span>
                        </div>
                        <div style={styles.pill}>
                            <span style={styles.pillLabel}>Ny målandel</span>
                            <span style={styles.pillValue}>{value.toFixed(0)}%</span>
                        </div>
                    </div>

                    <input
                        type="range"
                        min={0}
                        max={100}
                        step={1}
                        value={value}
                        onChange={(e) => setValue(Number(e.target.value))}
                        style={styles.slider}
                    />

                    <div style={styles.tickRow}>
                        <span>0%</span>
                        <span>50%</span>
                        <span>100%</span>
                    </div>

                    <div style={matchesActual ? styles.messageSuccess : styles.messageWarning}>
                        {matchesActual
                            ? 'Målandelen matchar nuläget.'
                            : `Nuläge: ${actualPct.toFixed(1)}% av portföljen (Mål: ${value.toFixed(0)}%)`}
                    </div>

                    {error && <p style={styles.error}>{error}</p>}

                    <div style={styles.buttonRow}>
                        <button style={styles.cancelBtn} onClick={onClose} disabled={saving}>
                            Avbryt
                        </button>
                        <button style={styles.saveBtn} onClick={handleSave} disabled={saving}>
                            {saving ? 'Sparar…' : 'Spara ny allokering'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RebalanceModal;

const styles: Record<string, React.CSSProperties> = {
    overlay: {
        position: 'fixed',
        inset: 0,
        zIndex: 60,
        backgroundColor: 'rgba(0, 0, 0, 0.4)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    card: {
        width: '100%',
        maxWidth: '420px',
        backgroundColor: '#FFFFFF',
        borderRadius: '16px',
        overflow: 'hidden',
        boxShadow: '0 20px 40px rgba(0, 0, 0, 0.2)',
    },
    header: {
        backgroundColor: '#0b2540',
        padding: '16px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    headerTitle: {
        color: '#FFFFFF',
        fontSize: '15px',
        fontWeight: 600,
    },
    closeBtn: {
        background: 'none',
        border: 'none',
        color: '#FFFFFF',
        fontSize: '20px',
        cursor: 'pointer',
        lineHeight: 1,
    },
    body: {
        padding: '24px',
    },
    heading: {
        margin: '0 0 16px 0',
        fontSize: '16px',
        fontWeight: 600,
        color: '#0F172A',
    },
    assetSplitBox: {
        backgroundColor: '#F8FAFC',
        border: '1px solid #E2E8F0',
        borderRadius: '10px',
        padding: '10px 14px',
        marginBottom: '16px',
    },
    assetSplitRow: {
        margin: '2px 0',
        fontSize: '13px',
        color: '#334155',
    },
    pillRow: {
        display: 'flex',
        gap: '12px',
        marginBottom: '16px',
    },
    pill: {
        flex: 1,
        backgroundColor: '#F1F5F9',
        borderRadius: '10px',
        padding: '10px 14px',
        display: 'flex',
        flexDirection: 'column',
        gap: '2px',
    },
    pillLabel: {
        fontSize: '12px',
        color: '#64748B',
    },
    pillValue: {
        fontSize: '18px',
        fontWeight: 700,
        color: '#0F172A',
    },
    slider: {
        width: '100%',
        marginBottom: '4px',
    },
    tickRow: {
        display: 'flex',
        justifyContent: 'space-between',
        fontSize: '11px',
        color: '#94A3B8',
        marginBottom: '16px',
    },
    messageSuccess: {
        backgroundColor: '#ECFDF5',
        color: '#059669',
        borderRadius: '8px',
        padding: '10px 12px',
        fontSize: '13px',
        marginBottom: '16px',
    },
    messageWarning: {
        backgroundColor: '#FFFBEB',
        color: '#B45309',
        borderRadius: '8px',
        padding: '10px 12px',
        fontSize: '13px',
        marginBottom: '16px',
    },
    error: {
        color: '#DC2626',
        fontSize: '13px',
        marginBottom: '12px',
    },
    buttonRow: {
        display: 'flex',
        gap: '12px',
        justifyContent: 'flex-end',
    },
    cancelBtn: {
        padding: '10px 16px',
        borderRadius: '8px',
        border: '1px solid #E2E8F0',
        backgroundColor: '#FFFFFF',
        color: '#334155',
        fontSize: '13px',
        fontWeight: 500,
        cursor: 'pointer',
    },
    saveBtn: {
        padding: '10px 16px',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#059669',
        color: '#FFFFFF',
        fontSize: '13px',
        fontWeight: 500,
        cursor: 'pointer',
    },
};
