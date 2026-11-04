import { useEffect, useState } from 'react';
import {
    applyRebalance,
    previewRebalance,
    type RebalancePreview,
} from '../../Services/rebalanceService';
import './RebalanceModal.css';

type AllocationSplit = {
    aktier: number;
    fonder: number;
};

type RebalanceModalProps = {
    accountId: number;
    actual: AllocationSplit;
    target: AllocationSplit;
    onClose: () => void;
    onApplied: () => void;
};

const formatSek = (value: number): string =>
    `${value.toLocaleString('sv-SE', { maximumFractionDigits: 0 })} SEK`;

const RebalanceModal = ({ accountId, actual, target, onClose, onApplied }: RebalanceModalProps) => {
    // The slider is the Aktier share. It starts at the account's current split.
    const [value, setValue] = useState(Math.round(actual.aktier));
    const [preview, setPreview] = useState<RebalancePreview | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [applying, setApplying] = useState(false);

    // Fetches live measurements from the backend whenever the slider moves.
    useEffect(() => {
        const controller = new AbortController();
        const timer = setTimeout(() => {
            previewRebalance(accountId, value, controller.signal)
                .then((data) => {
                    setPreview(data);
                    setError(null);
                })
                .catch((err) => {
                    if (!controller.signal.aborted) {
                        setPreview(null);
                        setError(err instanceof Error ? err.message : 'Något gick fel.');
                    }
                });
        }, 200);

        return () => {
            clearTimeout(timer);
            controller.abort();
        };
    }, [accountId, value]);

    const handleApply = async () => {
        setApplying(true);
        setError(null);

        try {
            await applyRebalance(accountId, value);
            onApplied();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Kunde inte genomföra rebalanseringen.');
        } finally {
            setApplying(false);
        }
    };

    const trades = preview?.trades ?? [];
    const movedValue = trades
        .filter((trade) => trade.side === 'SELL')
        .reduce((total, trade) => total + trade.value, 0);

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
                    <div style={styles.assetSplitBox}>
                        <p style={styles.assetSplitRow}>
                            <strong>
                                Mål: {target.aktier}% Aktier / {target.fonder}% Fonder
                            </strong>
                        </p>
                        <p style={styles.assetSplitRow}>
                            <strong>
                                Nuvarande: {actual.aktier}% Aktier / {actual.fonder}% Fonder
                            </strong>
                        </p>
                    </div>

                    <input
                        type="range"
                        className="rebalance-slider"
                        min={0}
                        max={100}
                        step={1}
                        value={value}
                        onChange={(e) => setValue(Number(e.target.value))}
                        aria-label="Andel aktier"
                    />

                    <div style={styles.sliderLabels}>
                        <span>Aktier {value}%</span>
                        <span>Fonder {100 - value}%</span>
                    </div>

                    <div style={styles.liveBox}>
                        <div style={styles.liveTitle}>Live-beräkning</div>

                        {trades.length > 0 ? (
                            <>
                                <table style={styles.table}>
                                    <tbody>
                                        {trades.map((trade) => (
                                            <tr key={`${trade.holdingId}-${trade.side}`}>
                                                <td style={trade.side === 'SELL' ? styles.sell : styles.buy}>
                                                    {trade.side === 'SELL' ? 'Sälj' : 'Köp'}
                                                </td>
                                                <td>{trade.ticker}</td>
                                                <td style={styles.right}>{trade.quantity} st</td>
                                                <td style={styles.right}>{formatSek(trade.value)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                                <div style={styles.moved}>Flyttas: {formatSek(movedValue)}</div>
                            </>
                        ) : (
                            !error && (
                                <p style={styles.muted}>
                                    {preview?.warning ?? 'Inga köp eller sälj behövs för detta val.'}
                                </p>
                            )
                        )}
                    </div>

                    {error && <p style={styles.error}>{error}</p>}

                    <div style={styles.buttonRow}>
                        <button style={styles.cancelBtn} onClick={onClose} disabled={applying}>
                            Avbryt
                        </button>
                        <button
                            style={styles.saveBtn}
                            onClick={handleApply}
                            disabled={applying || trades.length === 0}
                        >
                            {applying ? 'Rebalanserar…' : 'Rebalansera'}
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
    headerTitle: { color: '#FFFFFF', fontSize: '15px', fontWeight: 600 },
    closeBtn: {
        background: 'none',
        border: 'none',
        color: '#FFFFFF',
        fontSize: '20px',
        cursor: 'pointer',
        lineHeight: 1,
    },
    body: { padding: '24px' },
    assetSplitBox: {
        backgroundColor: '#F8FAFC',
        border: '1px solid #E2E8F0',
        borderRadius: '10px',
        padding: '10px 14px',
        marginBottom: '16px',
    },
    assetSplitRow: { margin: '2px 0', fontSize: '13px', color: '#334155' },
    sliderLabels: {
        display: 'flex',
        justifyContent: 'space-between',
        fontSize: '14px',
        fontWeight: 600,
        color: '#0F172A',
        marginBottom: '16px',
    },
    liveBox: {
        backgroundColor: '#F8FAFC',
        border: '1px solid #E2E8F0',
        borderRadius: '10px',
        padding: '12px 14px',
        marginBottom: '16px',
    },
    liveTitle: { fontSize: '12px', fontWeight: 600, color: '#64748B', marginBottom: '8px' },
    table: { width: '100%', fontSize: '13px', color: '#334155', borderCollapse: 'collapse' },
    right: { textAlign: 'right' },
    sell: { color: '#DC2626', fontWeight: 600 },
    buy: { color: '#059669', fontWeight: 600 },
    moved: { marginTop: '8px', fontSize: '13px', fontWeight: 600, color: '#0F172A' },
    muted: { margin: 0, fontSize: '13px', color: '#64748B' },
    error: { color: '#DC2626', fontSize: '13px', marginBottom: '12px' },
    buttonRow: { display: 'flex', gap: '12px', justifyContent: 'flex-end' },
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
