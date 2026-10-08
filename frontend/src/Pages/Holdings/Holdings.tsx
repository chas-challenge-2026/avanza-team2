import { useState, useEffect } from 'react';
import SearchInput from "../../Components/SearchInput/SearchInput";
import { DropdownBtn, type DropdownOption } from "../../Components/DropdownBtn/DropdownBtn";
import HoldingRow, { type HoldingItem } from "../../Components/HoldingRow/HoldingRow";
import "./Holdings.css";

// Alternativ för konton
const ACCOUNT_OPTIONS: DropdownOption[] = [
    { label: 'Alla konton', value: 'all' },
    { label: 'ISK', value: 'isk' },
    { label: 'KF', value: 'kf' },
    { label: 'Depå', value: 'depa' },
];

// Alternativ för valutor
const CURRENCY_OPTIONS: DropdownOption[] = [
    { label: 'SEK', value: 'sek' },
    { label: 'EUR', value: 'eur' },
    { label: 'USD', value: 'usd' },
];

const Holdings = () => {
    // 1. States för data, laddning och felhantering
    const [holdings, setHoldings] = useState<HoldingItem[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // 2. States för filter och sök
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedAccount, setSelectedAccount] = useState('all');
    const [selectedCurrency, setSelectedCurrency] = useState('sek');

    // 3. Hämta data (Här byter du ut mot ert riktiga API-anrop sen, t.ex. fetch('/api/holdings'))
    useEffect(() => {
        const fetchHoldings = async () => {
            try {
                setLoading(true);
                // --- BÖRJAN PÅ DET SOM BYTS UT MOT API SEN ---
                // Simulerar nätverksfördröjning
                await new Date(); // bara för att illustrera async
                const mockData: HoldingItem[] = [
                    { id: '1', symbol: 'ERIC-B', name: 'Ericsson B', shares: 500, value: 37100, returnPercent: 8.32 },
                    { id: '2', symbol: 'VOLV-B', name: 'Volvo B', shares: 100, value: 26850, returnPercent: 9.59 },
                    { id: '3', symbol: 'AAPL', name: 'Apple Inc', shares: 50, value: 97674.7, returnPercent: 13.53 },
                    { id: '4', symbol: 'SWED-A', name: 'Swedbank A', shares: 200, value: 38620, returnPercent: 4.38 },
                    { id: '5', symbol: 'SAND', name: 'Sandvik', shares: 200, value: 63840, returnPercent: 3.69 },
                ];
                setHoldings(mockData);
                // --- SLUT PÅ DET SOM BYTS UT ---
            } catch (err) {
                setError('Kunde inte hämta innehav. Försök igen senare.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchHoldings();
    }, [selectedAccount, selectedCurrency]); // Om du vill skicka med konton/valuta till API:et kan du lägga till dem här i beroendelistan!

    // Filtrera innehav baserat på sökning (och eventuellt konto/valuta när API-filtren är på plats)
    const filteredHoldings = holdings.filter(
        (item) =>
            item.symbol.toLowerCase().includes(searchQuery.toLowerCase()) ||
            item.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="holdings-container">
            <h1 className="holdings-title">Mina Innehav</h1>

            {/* Sökfält och filterknappar */}
            <div className="holdings-filter-section">
                <SearchInput value={searchQuery} onChange={setSearchQuery} />
                
                <div className="holdings-filter-buttons">
                    <DropdownBtn 
                        options={ACCOUNT_OPTIONS}
                        value={selectedAccount}
                        onChange={setSelectedAccount}
                        placeholder="Välj konto"
                    />
                    <DropdownBtn 
                        options={CURRENCY_OPTIONS}
                        value={selectedCurrency}
                        onChange={setSelectedCurrency}
                        placeholder="Valuta"
                    />
                </div>
            </div>

            {/* KPI-kort */}
            <div className="holdings-kpi-grid">
                <div className="holdings-kpi-card">
                    <span className="holdings-kpi-label">Totalt värde</span>
                    <div className="holdings-kpi-value">264 284,7 SEK</div>
                    <div className="holdings-kpi-trend positive">↑ 1,23% idag</div>
                </div>
                <div className="holdings-kpi-card">
                    <span className="holdings-kpi-label">Avkastning (SEK)</span>
                    <div className="holdings-kpi-value">+38 620</div>
                    <div className="holdings-kpi-trend positive">↑ 1,23%</div>
                </div>
            </div>

            {/* Innehav-lista med stöd för laddning och fel */}
            <div className="holdings-section-box">
                <h3 className="holdings-section-title">Innehav</h3>
                
                {loading && (
                    <div style={{ padding: '30px', textAlign: 'center', color: '#64748B', fontSize: '14px' }}>
                        Laddar innehav...
                    </div>
                )}

                {error && (
                    <div style={{ padding: '30px', textAlign: 'center', color: '#DC2626', fontSize: '14px' }}>
                        {error}
                    </div>
                )}

                {!loading && !error && filteredHoldings.length === 0 && (
                    <div style={{ padding: '30px', textAlign: 'center', color: '#64748B', fontSize: '14px' }}>
                        Inga innehav hittades.
                    </div>
                )}

                {!loading && !error && (
                    <div>
                        {filteredHoldings.map((item) => (
                            <HoldingRow 
                                key={item.id} 
                                item={item} 
                                onClick={(id) => console.log('Klickade på innehav:', id)} 
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Total innehav */}
            <div className="holdings-section-box">
                <h3 className="holdings-section-title">Total innehav</h3>
                <div className="holdings-summary-content">
                    <div className="summary-row">
                        <span className="summary-label">Värde totalt</span>
                        <span className="summary-value">264 284 SEK</span>
                    </div>
                    <div className="summary-row">
                        <span className="summary-label">Avkastning (SEK)</span>
                        <span className="summary-value positive">+38 620 SEK</span>
                    </div>
                    <div className="summary-row" style={{ borderBottom: 'none' }}>
                        <span className="summary-label">Avkastning (%)</span>
                        <span className="summary-value positive">1,23%</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Holdings;