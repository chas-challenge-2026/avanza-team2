import { useEffect, useState } from 'react';
import {
    applyRebalance,
    previewRebalance,
    type RebalancePreview,
} from '../../Services/rebalanceService';

export interface RebalanceAccount {
    id: number;
    account_type: string;
    account_name: string;
}

type Props = {
    accounts: RebalanceAccount[];
    // Called after a rebalance is applied so the page can reload its holdings.
    onApplied: () => void;
};

// Default 50/50 target used to detect drift for an account.
const DEFAULT_TARGET_AKTIER_PCT = 50;

const pct = (value: number) => `${value.toFixed(1)}%`;

const accountLabel = (account: RebalanceAccount) =>
    account.account_name || (account.account_type === 'Depa' ? 'Depå' : account.account_type);

const AccountRebalanceCard = ({ accounts, onApplied }: Props) => {
    const [previews, setPreviews] = useState<Record<number, RebalancePreview>>({});
    const [selected, setSelected] = useState<RebalanceAccount | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    // Loads current split and drift for every account.
    useEffect(() => {
        const controller = new AbortController();

        Promise.all(
            accounts.map((account) =>
                previewRebalance(account.id, DEFAULT_TARGET_AKTIER_PCT, controller.signal)
                    .then((preview) => [account.id, preview] as const)
                    .catch(() => null),
            ),
        ).then((results) => {
            if (controller.signal.aborted) {
                return;
            }
            const next: Record<number, RebalancePreview> = {};
            results.forEach((entry) => {
                if (entry) {
                    next[entry[0]] = entry[1];
                }
            });
            setPreviews(next);
        });

        return () => controller.abort();
    }, [accounts, refreshKey]);

    const rows = accounts.filter((account) => previews[account.id]);

    return (
        <div style={styles.card}>
            <h3 style={styles.title}>Rebalansering</h3>

            {rows.length === 0 && <p style={styles.muted}>Inga konton att rebalansera.</p>}

            {rows.map((account) => {
                const preview = previews[account.id];
                return (
                    <div key={account.id} style={styles.row}>
                        <div>
                            <div style={styles.name}>{accountLabel(account)}</div>
                            <div style={styles.muted}>
                                {pct(preview.currentAktierPct)} Aktier / {pct(preview.currentFonderPct)} Fonder
                            </div>
                        </div>
                        {preview.drifted && (
                            <button style={styles.primaryBtn} onClick={() => setSelected(account)}>
                                Rebalansera
                            </button>
                        )}
                    </div>
                );
            })}

            {selected && previews[selected.id] && (
                <RebalancePopup
                    account={selected}
                    initial={previews[selected.id]}
                    onClose={() => setSelected(null)}
                    onApplied={() => {
                        setSelected(null);
                        setRefreshKey((key) => key + 1);
                        onApplied();
                    }}
                />
            )}
        </div>
    );
};

type PopupProps = {
    account: RebalanceAccount;
    initial: RebalancePreview;
    onClose: () => void;
    onApplied: () => void;
};

const RebalancePopup = ({ account, initial, onClose, onApplied }: PopupProps) => {
    // Slider is the Aktier share: Aktier on the left, Fonder on the right.
    const [target, setTarget] = useState(Math.round(initial.currentAktierPct));
    const [preview, setPreview] = useState<RebalancePreview | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [saving, setSaving] = useState(false);

    // Re-calculates the proposed trades whenever the slider moves.
    useEffect(() => {
        const controller = new AbortController();
        const timer = setTimeout(() => {
            previewRebalance(account.id, target, controller.signal)
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
    }, [account.id, target]);

    const handleApply = async () => {
        setSaving(true);
        setError(null);
        try {
            await applyRebalance(account.id, target);
            onApplied();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Något gick fel.');
        } finally {
            setSaving(false);
        }
    };

    const trades = preview?.trades ?? [];

    return (
        <div style={styles.overlay}>
            <div style={styles.popup}>
                <div style={styles.header}>
                    <span>Rebalansera {accountLabel(account)}</span>
                    <button style={styles.close} onClick={onClose} aria-label="Stäng">
                        ×
                    </button>
                </div>

                <div style={styles.body}>
                    <p style={styles.muted}>
                        Nuläge: {pct(initial.currentAktierPct)} Aktier / {pct(initial.currentFonderPct)} Fonder
                    </p>

                    <div style={styles.sliderLabels}>
                        <strong>Aktier {target}%</strong>
                        <strong>Fonder {100 - target}%</strong>
                    </div>
                    <input
                        type="range"
                        min={0}
                        max={100}
                        step={1}
                        value={target}
                        onChange={(e) => setTarget(Number(e.target.value))}
                        style={{ width: '100%' }}
                    />

                    {trades.length > 0 ? (
                        <table style={styles.table}>
                            <tbody>
                                {trades.map((trade) => (
                                    <tr key={`${trade.holdingId}-${trade.side}`}>
                                        <td>{trade.side === 'SELL' ? 'Sälj' : 'Köp'}</td>
                                        <td>{trade.ticker}</td>
                                        <td style={{ textAlign: 'right' }}>{trade.quantity} st</td>
                                        <td style={{ textAlign: 'right' }}>
                                            {trade.value.toLocaleString('sv-SE')} SEK
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        !error && (
                            <p style={styles.muted}>
                                {preview?.warning ?? 'Inga köp eller sälj behövs för detta val.'}
                            </p>
                        )
                    )}

                    {error && <p style={styles.error}>{error}</p>}

                    <div style={styles.buttons}>
                        <button style={styles.secondaryBtn} onClick={onClose} disabled={saving}>
                            Avbryt
                        </button>
                        <button
                            style={styles.primaryBtn}
                            onClick={handleApply}
                            disabled={saving || trades.length === 0}
                        >
                            {saving ? 'Rebalanserar…' : 'Rebalansera'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AccountRebalanceCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        backgroundColor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '16px',
        padding: '20px',
    },
    title: { margin: '0 0 12px 0', fontSize: '16px', fontWeight: 600, color: '#0F172A' },
    row: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 0',
        borderTop: '1px solid #F1F5F9',
    },
    name: { fontWeight: 600, color: '#0F172A' },
    muted: { margin: 0, fontSize: '13px', color: '#64748B' },
    error: { color: '#DC2626', fontSize: '13px' },
    primaryBtn: {
        backgroundColor: '#0b2540',
        color: '#FFFFFF',
        border: 'none',
        borderRadius: '8px',
        padding: '8px 16px',
        cursor: 'pointer',
    },
    secondaryBtn: {
        backgroundColor: '#F1F5F9',
        color: '#0F172A',
        border: 'none',
        borderRadius: '8px',
        padding: '8px 16px',
        cursor: 'pointer',
    },
    overlay: {
        position: 'fixed',
        inset: 0,
        zIndex: 60,
        backgroundColor: 'rgba(0, 0, 0, 0.4)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    popup: {
        width: '100%',
        maxWidth: '460px',
        backgroundColor: '#FFFFFF',
        borderRadius: '16px',
        overflow: 'hidden',
    },
    header: {
        backgroundColor: '#0b2540',
        color: '#FFFFFF',
        padding: '16px 20px',
        display: 'flex',
        justifyContent: 'space-between',
        fontWeight: 600,
    },
    close: { background: 'none', border: 'none', color: '#FFFFFF', fontSize: '20px', cursor: 'pointer' },
    body: { padding: '20px' },
    sliderLabels: { display: 'flex', justifyContent: 'space-between', margin: '12px 0 4px' },
    table: { width: '100%', fontSize: '13px', margin: '16px 0' },
    buttons: { display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' },
};
