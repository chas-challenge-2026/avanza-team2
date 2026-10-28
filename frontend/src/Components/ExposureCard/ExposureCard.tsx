import React from 'react';

const SECTOR_COLORS = {
    tech: '#10B981',      
    industry: '#F59E0B',  
    finance: '#8B5CF6',   
};

const ExposureCard = () => {
    return (
        <div style={styles.card}>
            <h3 style={styles.title}>Exponering & Spridning</h3>

            {/* Geografisk spridning (Länder) */}
            <div style={styles.section}>
                <span style={styles.sectionLabel}>Största länder</span>
                <div style={styles.tagGrid}>
                    <div style={styles.tag}>
                        <span style={styles.flag}>🇸🇪</span>
                        <span style={styles.tagName}>Sverige</span>
                        <span style={styles.tagPercent}>50%</span>
                    </div>
                    <div style={styles.tag}>
                        <span style={styles.flag}>🇺🇸</span>
                        <span style={styles.tagName}>USA</span>
                        <span style={styles.tagPercent}>30%</span>
                    </div>
                    <div style={styles.tag}>
                        <span style={styles.flag}>🌍</span>
                        <span style={styles.tagName}>Globalt</span>
                        <span style={styles.tagPercent}>20%</span>
                    </div>
                </div>
            </div>

            {/* Branschexponering med helt unika färger */}
            <div style={{ ...styles.section, marginBottom: 0 }}>
                <span style={styles.sectionLabel}>Största branscher</span>
                <div style={styles.branchList}>
                    {/* Bransch 1: Teknik (Smaragdgrön) */}
                    <div style={styles.branchRow}>
                        <span style={styles.branchName}>💻 Teknik</span>
                        <div style={styles.branchTrack}>
                            <div style={{ ...styles.branchFill, width: '40%', backgroundColor: SECTOR_COLORS.tech }} />
                        </div>
                        <span style={styles.branchPercent}>40%</span>
                    </div>

                    {/* Bransch 2: Industri (Orange/Bärnsten) */}
                    <div style={styles.branchRow}>
                        <span style={styles.branchName}>🏭 Industri</span>
                        <div style={styles.branchTrack}>
                            <div style={{ ...styles.branchFill, width: '25%', backgroundColor: SECTOR_COLORS.industry }} />
                        </div>
                        <span style={styles.branchPercent}>25%</span>
                    </div>

                    {/* Bransch 3: Finans (Violett/Lila) */}
                    <div style={styles.branchRow}>
                        <span style={styles.branchName}>🏦 Finans</span>
                        <div style={styles.branchTrack}>
                            <div style={{ ...styles.branchFill, width: '20%', backgroundColor: SECTOR_COLORS.finance }} />
                        </div>
                        <span style={styles.branchPercent}>20%</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExposureCard;

const styles: Record<string, React.CSSProperties> = {
    card: {
        backgroundColor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '12px',
        padding: '20px',
        boxSizing: 'border-box',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    title: {
        margin: '0 0 16px 0',
        fontSize: '16px',
        fontWeight: 600,
        color: '#0F172A',
    },
    section: {
        marginBottom: '18px',
    },
    sectionLabel: {
        fontSize: '13px',
        fontWeight: 600,
        color: '#334155',
        display: 'block',
        marginBottom: '8px',
    },
    tagGrid: {
        display: 'flex',
        gap: '8px',
        marginTop: '4px',
    },
    tag: {
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        padding: '8px 10px',
        backgroundColor: '#F8FAFC',
        border: '1px solid #E2E8F0',
        borderRadius: '8px',
        fontSize: '12px',
    },
    flag: {
        fontSize: '14px',
    },
    tagName: {
        color: '#334155',
        fontWeight: 500,
        flex: 1,
    },
    tagPercent: {
        color: '#0F172A',
        fontWeight: 600,
    },
    branchList: {
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
        marginTop: '6px',
    },
    branchRow: {
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        fontSize: '12px',
    },
    branchName: {
        width: '85px',
        color: '#475569',
        fontWeight: 500,
    },
    branchTrack: {
        flex: 1,
        height: '6px',
        backgroundColor: '#F1F5F9',
        borderRadius: '3px',
        overflow: 'hidden',
    },
    branchFill: {
        height: '100%',
        borderRadius: '3px',
        transition: 'width 0.3s ease',
    },
    branchPercent: {
        width: '30px',
        textAlign: 'right',
        color: '#64748B',
        fontWeight: 600,
    },
};